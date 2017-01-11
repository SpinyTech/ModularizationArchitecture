package com.spinytech.macore.router;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.Log;

import com.spinytech.macore.ErrorAction;
import com.spinytech.macore.IWideRouterAIDL;
import com.spinytech.macore.MaAction;
import com.spinytech.macore.MaActionResult;
import com.spinytech.macore.MaApplication;
import com.spinytech.macore.MaProvider;
import com.spinytech.macore.tools.Logger;
import com.spinytech.macore.tools.ProcessUtil;

import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * The Local Router
 */

public class LocalRouter {
    private static final String TAG = "LocalRouter";
    private String mProcessName = ProcessUtil.UNKNOWN_PROCESS_NAME;
    private static LocalRouter sInstance = null;
    private HashMap<String, MaProvider> mProviders = null;
    private MaApplication mApplication;
    private IWideRouterAIDL mWideRouterAIDL;
    private static ExecutorService threadPool = null;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e(TAG,"SER"+service);
            mWideRouterAIDL = IWideRouterAIDL.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mWideRouterAIDL = null;
        }
    };

    private LocalRouter(MaApplication context) {
        mApplication = context;
        mProcessName = ProcessUtil.getProcessName(context, ProcessUtil.getMyProcessId());
        mProviders = new HashMap<>();
        if(mApplication.needMultipleProcess()&&!WideRouter.PROCESS_NAME.equals(mProcessName)){
            connectWideRouter();
        }
    }

    public static synchronized LocalRouter getInstance(@NonNull MaApplication context) {
        if (sInstance == null) {
            sInstance = new LocalRouter(context);
        }
        return sInstance;
    }

    private static synchronized ExecutorService getThreadPool() {
        if (null == threadPool) {
            threadPool = Executors.newCachedThreadPool();
        }
        return threadPool;
    }

    void connectWideRouter() {
        Intent binderIntent = new Intent(mApplication, WideRouterConnectService.class);
        binderIntent.putExtra("domain",mProcessName);
        mApplication.bindService(binderIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    void disconnectWideRouter() {
        if(null == mServiceConnection){
            return;
        }
        mApplication.unbindService(mServiceConnection);
        mWideRouterAIDL = null;
    }

    public void registerProvider(String providerName, MaProvider provider) {
        mProviders.put(providerName, provider);
    }


    private boolean checkWideRouterConnection() {
        boolean result = false;
        if (mWideRouterAIDL != null) {
            result = true;
        }
        return result;
    }

    boolean answerWiderAsync(@NonNull RouterRequest routerRequest) {
        if (mProcessName.equals(routerRequest.getDomain()) && checkWideRouterConnection()) {
            return findRequestAction(routerRequest).isAsync(mApplication, routerRequest.getData());
        } else {
            return true;
        }
    }

    public RouterResponse route(Context context, @NonNull RouterRequest routerRequest) throws Exception {
        Logger.d(TAG, "Process:" + mProcessName + "\nLocal route start: " + System.currentTimeMillis() + "\nRequest:" + routerRequest.toString());
        RouterResponse routerResponse = new RouterResponse();
        // Local request
        if (mProcessName.equals(routerRequest.getDomain())) {
            MaAction targetAction = findRequestAction(routerRequest);
            routerResponse.mIsAsync = targetAction.isAsync(context, routerRequest.getData());
            // Sync result, return the result immediately.
            if (!routerResponse.mIsAsync) {
                MaActionResult result = targetAction.invoke(context, routerRequest.getData());
                routerResponse.mResultString = result.toString();
                routerResponse.mObject = result.getObject();
                Logger.d(TAG, "Process:" + mProcessName + "\nLocal sync end: " + System.currentTimeMillis() + "\nRequest:" + routerRequest.toString());
            }
            // Async result, use the thread pool to execute the task.
            else {
                LocalTask task = new LocalTask(routerResponse, routerRequest, context, targetAction);
                routerResponse.mAsyncResponse = getThreadPool().submit(task);
            }
        } else if (!mApplication.needMultipleProcess()) {
            throw new Exception("Please make sure the returned value of needMultipleProcess in MaApplication is true, so that you can invoke other process action.");
        }
        // IPC request
        else {
            // Because AIDL always block about 100ms, so the wide routing is always asynchronous.
            // And IWideRouterAIDL.checkResponseAsync now is deprecated.
            routerResponse.mIsAsync = true;
            // Has connected with wide router
            if (!checkWideRouterConnection()) {
                ConnectWideTask task = new ConnectWideTask(routerResponse, routerRequest);
                routerResponse.mAsyncResponse = getThreadPool().submit(task);
            }
            // Has not connected with the wide router.
            else {
                WideTask task = new WideTask(routerRequest);
                routerResponse.mAsyncResponse = getThreadPool().submit(task);
            }
        }
        return routerResponse;
    }

    public boolean stopSelf(Class<? extends LocalRouterConnectService> clazz) {
        if (checkWideRouterConnection()) {
            try {
                if (mWideRouterAIDL.stopRouter(mProcessName)) {
                    disconnectWideRouter();
                    return true;
                } else {
                    return false;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            mApplication.stopService(new Intent(mApplication, clazz));
            return true;
        }
    }

    public void stopWideRouter(){
        if (checkWideRouterConnection()) {
            try {
                mWideRouterAIDL.stopRouter(WideRouter.PROCESS_NAME);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            Logger.e(TAG,"This local router hasn't connected the wide router.");
        }
    }

    private MaAction findRequestAction(RouterRequest routerRequest) {
        MaProvider targetProvider = mProviders.get(routerRequest.getProvider());
        ErrorAction defaultNotFoundAction = new ErrorAction();
        if (null == targetProvider) {
            return defaultNotFoundAction;
        } else {
            MaAction targetAction = targetProvider.findAction(routerRequest.getAction());
            if (null == targetAction) {
                return defaultNotFoundAction;
            } else {
                return targetAction;
            }
        }
    }

    private class LocalTask implements Callable<String> {
        private RouterResponse mResponse;
        private RouterRequest mRequest;
        private Context mContext;
        private MaAction mAction;

        public LocalTask(RouterResponse routerResponse, RouterRequest routerRequest, Context context, MaAction maAction) {
            this.mContext = context;
            this.mResponse = routerResponse;
            this.mRequest = routerRequest;
            this.mAction = maAction;
        }

        @Override
        public String call() throws Exception {
            MaActionResult result = mAction.invoke(mContext, mRequest.getData());
            mResponse.mObject = result.getObject();
            Logger.d(TAG, "Process:" + mProcessName + "\nLocal async end: " + System.currentTimeMillis() + "\nRequest:" + mRequest.toString());
            return result.toString();
        }
    }

    private class WideTask implements Callable<String> {
        private RouterRequest mRequest;

        public WideTask(RouterRequest routerRequest) {
            this.mRequest = routerRequest;
        }

        @Override
        public String call() throws Exception {
            String result = mWideRouterAIDL.route(mRequest.toString());
            Logger.d(TAG, "Process:" + mProcessName + "\nWide async end: " + System.currentTimeMillis() + "\nRequest:" + mRequest.toString());
            return result;
        }
    }

    private class ConnectWideTask implements Callable<String> {
        private RouterRequest mRequest;
        private RouterResponse mResponse;

        public ConnectWideTask(RouterResponse routerResponse, RouterRequest routerRequest) {
            this.mRequest = routerRequest;
            this.mResponse = routerResponse;
        }

        @Override
        public String call() throws Exception {
            Logger.d(TAG, "Process:" + mProcessName + "\nBind wide router start: " + System.currentTimeMillis() + "\nRequest:" + mRequest.toString());
            connectWideRouter();
            int time = 0;
            while (true) {
                if (null == mWideRouterAIDL) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    time++;
                } else {
                    break;
                }
                if (time >= 600) {
                    ErrorAction defaultNotFoundAction = new ErrorAction(true, MaActionResult.CODE_CANNOT_BIND_WIDE, "Bind wide router time out. Can not bind wide router.");
                    MaActionResult result = defaultNotFoundAction.invoke(mApplication, mRequest.getData());
                    mResponse.mResultString = result.toString();
                    return result.toString();
                }
            }
            Logger.d(TAG, "Process:" + mProcessName + "\nBind wide router end: " + System.currentTimeMillis() + "\nRequest:" + mRequest.toString());
            String result = mWideRouterAIDL.route(mRequest.toString());
            Logger.d(TAG, "Process:" + mProcessName + "\nWide async end: " + System.currentTimeMillis() + "\nRequest:" + mRequest.toString());
            return result;
        }
    }
}
