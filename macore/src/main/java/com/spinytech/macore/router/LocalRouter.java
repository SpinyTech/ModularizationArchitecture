package com.spinytech.macore.router;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;

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

import io.reactivex.Observable;

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
        if (mApplication.needMultipleProcess() && !WideRouter.PROCESS_NAME.equals(mProcessName)) {
            connectWideRouter();
        }
    }

    public static synchronized LocalRouter getInstance(@NonNull MaApplication context) {
        if (sInstance == null) {
            sInstance = new LocalRouter(context);
        }
        return sInstance;
    }

    private static ExecutorService getThreadPool() {
        if (null == threadPool) {
            synchronized (LocalRouter.class) {
                threadPool = Executors.newCachedThreadPool();
            }
        }
        return threadPool;
    }

    public void connectWideRouter() {
        Intent binderIntent = new Intent(mApplication, WideRouterConnectService.class);
        binderIntent.putExtra("domain", mProcessName);
        mApplication.bindService(binderIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    public void disconnectWideRouter() {
        if (null == mServiceConnection) {
            return;
        }
        mApplication.unbindService(mServiceConnection);
        mWideRouterAIDL = null;
    }

    public void registerProvider(String providerName, MaProvider provider) {
        mProviders.put(providerName, provider);
    }


    public boolean checkWideRouterConnection() {
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

    public MaActionResult wideRoute(Context context, @NonNull RouterRequest routerRequest) throws Exception {
        Logger.d(TAG, "Process:" + mProcessName + "\nLocal route start: " + System.currentTimeMillis());
        // Local request
        if (mProcessName.equals(routerRequest.getDomain())) {
            HashMap<String, String> params = new HashMap<>();
            params.putAll(routerRequest.getData());
            Logger.d(TAG, "Process:" + mProcessName + "\nLocal find action start: " + System.currentTimeMillis());
            MaAction targetAction = findRequestAction(routerRequest);
            routerRequest.isIdle.set(true);
            Logger.d(TAG, "Process:" + mProcessName + "\nLocal find action end: " + System.currentTimeMillis());
            boolean mIsAsync = targetAction.isAsync(context, params);
            // Sync result, return the result immediately.
            if (!mIsAsync) {
                Logger.d(TAG, "Process:" + mProcessName + "\nLocal sync end: " + System.currentTimeMillis());
                return targetAction.invoke(context, params);
            }
            // Async result, use the thread pool to execute the task.
            else {
                LocalTask task = new LocalTask(params, context, targetAction);
                return getThreadPool().submit(task).get();
            }
        } else if (!mApplication.needMultipleProcess()) {
            throw new Exception("Please make sure the returned value of needMultipleProcess in MaApplication is true, so that you can invoke other process action.");
        }
        // IPC request
        else {
            String domain = routerRequest.getDomain();
            String routerRequestString = routerRequest.toString();
            routerRequest.isIdle.set(true);
            boolean mIsAsync = false;
            if (checkWideRouterConnection()) {
                Logger.d(TAG, "Process:" + mProcessName + "\nWide async check start: " + System.currentTimeMillis());
                //If you don't need wide async check, use "routerResponse.mIsAsync = false;" replace the next line to improve performance.
                mIsAsync = mWideRouterAIDL.checkResponseAsync(domain, routerRequestString);
                Logger.d(TAG, "Process:" + mProcessName + "\nWide async check end: " + System.currentTimeMillis());
                if (!mIsAsync) {
                    Logger.d(TAG, "Process:" + mProcessName + "\nWide sync end: " + System.currentTimeMillis());
                    return mWideRouterAIDL.route(domain, routerRequestString);
                }
                // Async result, use the thread pool to execute the task.
                else {
                    WideTask task = new WideTask(domain, routerRequestString);
                    return getThreadPool().submit(task).get();
                }
            }
            // Has not connected with the wide router.
            else {
                ConnectWideTask task = new ConnectWideTask(domain, routerRequestString);
                return getThreadPool().submit(task).get();
            }
        }
    }

    public Observable<MaActionResult> route(Context context, @NonNull RouterRequest routerRequest) throws Exception {
        Logger.d(TAG, "Process:" + mProcessName + "\nLocal route start: " + System.currentTimeMillis());
        MaActionResult maActionResult = null;
        // Local request
        if (mProcessName.equals(routerRequest.getDomain())) {
            HashMap<String, String> params = new HashMap<>();
            params.putAll(routerRequest.getData());
            Logger.d(TAG, "Process:" + mProcessName + "\nLocal find action start: " + System.currentTimeMillis());
            MaAction targetAction = findRequestAction(routerRequest);
            routerRequest.isIdle.set(true);
            Logger.d(TAG, "Process:" + mProcessName + "\nLocal find action end: " + System.currentTimeMillis());
            // Sync result, return the result immediately.
            if (!targetAction.isAsync(context, params)) {
                maActionResult = targetAction.invoke(context, params);
                Logger.d(TAG, "Process:" + mProcessName + "\nLocal sync end: " + System.currentTimeMillis());
                return Observable.just(maActionResult);
            }
            // Async result, use the thread pool to execute the task.
            else {
                LocalTask task = new LocalTask(params, context, targetAction);
                return Observable.fromFuture(getThreadPool().submit(task));
            }
        } else if (!mApplication.needMultipleProcess()) {
            throw new Exception("Please make sure the returned value of needMultipleProcess in MaApplication is true, so that you can invoke other process action.");
        }
        // IPC request
        else {
            String domain = routerRequest.getDomain();
            String routerRequestString = routerRequest.toString();
            routerRequest.isIdle.set(true);
            boolean mIsAsync = false;
            if (checkWideRouterConnection()) {
                Logger.d(TAG, "Process:" + mProcessName + "\nWide async check start: " + System.currentTimeMillis());
                //If you don't need wide async check, use "maActionResult.mIsAsync = false;" replace the next line to improve performance.
                mIsAsync = mWideRouterAIDL.checkResponseAsync(domain, routerRequestString);
                Logger.d(TAG, "Process:" + mProcessName + "\nWide async check end: " + System.currentTimeMillis());
                if (!mIsAsync) {
                    Logger.d(TAG, "Process:" + mProcessName + "\nWide sync end: " + System.currentTimeMillis());
                    return Observable.just(mWideRouterAIDL.route(domain, routerRequestString));
                }
                // Async result, use the thread pool to execute the task.
                else {
                    WideTask task = new WideTask(domain, routerRequestString);
                    return Observable.fromFuture(getThreadPool().submit(task));
                }
            }
            // Has not connected with the wide router.
            else {
                ConnectWideTask task = new ConnectWideTask(domain, routerRequestString);
                return Observable.fromFuture(getThreadPool().submit(task));
            }
        }
    }

    public boolean stopSelf(Class<? extends LocalRouterConnectService> clazz) {
        if (checkWideRouterConnection()) {
            try {
                return mWideRouterAIDL.stopRouter(mProcessName);
            } catch (RemoteException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            mApplication.stopService(new Intent(mApplication, clazz));
            return true;
        }
    }

    public void stopWideRouter() {
        if (checkWideRouterConnection()) {
            try {
                mWideRouterAIDL.stopRouter(WideRouter.PROCESS_NAME);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            Logger.e(TAG, "This local router hasn't connected the wide router.");
        }
    }

    private MaAction findRequestAction(RouterRequest routerRequest) {
        MaProvider targetProvider = mProviders.get(routerRequest.getProvider());
        ErrorAction defaultNotFoundAction = new ErrorAction(false, MaActionResult.CODE_NOT_FOUND, "Not found the action.");
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

    private class LocalTask implements Callable<MaActionResult> {
        private HashMap<String, String> mRequestData;
        private Context mContext;
        private MaAction mAction;

        public LocalTask(HashMap<String, String> requestData, Context context, MaAction maAction) {
            this.mContext = context;
            this.mRequestData = requestData;
            this.mAction = maAction;
        }

        @Override
        public MaActionResult call() throws Exception {
            MaActionResult result = mAction.invoke(mContext, mRequestData);
            Logger.d(TAG, "Process:" + mProcessName + "\nLocal async end: " + System.currentTimeMillis());
            return result;
        }
    }

    private class WideTask implements Callable<MaActionResult> {

        private String mDomain;
        private String mRequestString;

        public WideTask(String domain, String requestString) {
            this.mDomain = domain;
            this.mRequestString = requestString;
        }

        @Override
        public MaActionResult call() throws Exception {
            Logger.d(TAG, "Process:" + mProcessName + "\nWide async start: " + System.currentTimeMillis());
            MaActionResult result = mWideRouterAIDL.route(mDomain, mRequestString);
            Logger.d(TAG, "Process:" + mProcessName + "\nWide async end: " + System.currentTimeMillis());
            return result;
        }
    }

    private class ConnectWideTask implements Callable<MaActionResult> {
        private String mDomain;
        private String mRequestString;

        public ConnectWideTask(String domain, String requestString) {
            this.mDomain = domain;
            this.mRequestString = requestString;
        }

        @Override
        public MaActionResult call() throws Exception {
            Logger.d(TAG, "Process:" + mProcessName + "\nBind wide router start: " + System.currentTimeMillis());
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
                    return defaultNotFoundAction.invoke(mApplication, new HashMap<String, String>());
                }
            }
            Logger.d(TAG, "Process:" + mProcessName + "\nBind wide router end: " + System.currentTimeMillis());
            MaActionResult result = mWideRouterAIDL.route(mDomain, mRequestString);
            Logger.d(TAG, "Process:" + mProcessName + "\nWide async end: " + System.currentTimeMillis());
            return result;
        }
    }
}
