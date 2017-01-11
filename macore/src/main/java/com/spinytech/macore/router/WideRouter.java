package com.spinytech.macore.router;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.spinytech.macore.ErrorAction;
import com.spinytech.macore.ILocalRouterAIDL;
import com.spinytech.macore.MaActionResult;
import com.spinytech.macore.MaApplication;
import com.spinytech.macore.tools.Logger;
import com.spinytech.macore.tools.ProcessUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * Created by wanglei on 2016/11/29.
 */

public class WideRouter {
    private static final String TAG = "WideRouter";
    public static final String PROCESS_NAME = "com.spiny.ma.widerouter";
    private static HashMap<String, ConnectServiceWrapper> sLocalRouterClasses;
    private static WideRouter sInstance = null;
    private MaApplication mApplication;
    private HashMap<String, ServiceConnection> mLocalRouterConnectionMap;
    private HashMap<String, ILocalRouterAIDL> mLocalRouterAIDLMap;
    boolean mIsStopping = false;

    private WideRouter(MaApplication context) {
        mApplication = context;
        String checkProcessName = ProcessUtil.getProcessName(context, ProcessUtil.getMyProcessId());
        if (!PROCESS_NAME.equals(checkProcessName)) {
            throw new RuntimeException("You should not initialize the WideRouter in process:" + checkProcessName);
        }
        sLocalRouterClasses = new HashMap<>();
        mLocalRouterConnectionMap = new HashMap<>();
        mLocalRouterAIDLMap = new HashMap<>();
    }

    public static synchronized WideRouter getInstance(@NonNull MaApplication context) {
        if (sInstance == null) {
            sInstance = new WideRouter(context);
        }
        return sInstance;
    }

    public static void registerLocalRouter(String processName, Class<? extends LocalRouterConnectService> targetClass) {
        if (null == sLocalRouterClasses) {
            sLocalRouterClasses = new HashMap<>();
        }
        ConnectServiceWrapper connectServiceWrapper = new ConnectServiceWrapper(targetClass);
        sLocalRouterClasses.put(processName, connectServiceWrapper);
    }

    boolean checkLocalRouterHasRegistered(final String domain){
        Class<? extends LocalRouterConnectService> clazz = sLocalRouterClasses.get(domain).targetClass;
        if (null == clazz) {
            return false;
        }else{
            return true;
        }
    }

    boolean connectLocalRouter(final String domain) {
        Class<? extends LocalRouterConnectService> clazz = sLocalRouterClasses.get(domain).targetClass;
        if (null == clazz) {
            return false;
        }
        Intent binderIntent = new Intent(mApplication, clazz);
        Bundle bundle = new Bundle();
        binderIntent.putExtras(bundle);
        final ServiceConnection serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                ILocalRouterAIDL mLocalRouterAIDL = ILocalRouterAIDL.Stub.asInterface(service);
                ILocalRouterAIDL temp = mLocalRouterAIDLMap.get(domain);
                if (null == temp) {
                    mLocalRouterAIDLMap.put(domain, mLocalRouterAIDL);
                    mLocalRouterConnectionMap.put(domain, this);
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mLocalRouterAIDLMap.remove(domain);
                mLocalRouterConnectionMap.remove(domain);
            }
        };
        mApplication.bindService(binderIntent, serviceConnection, BIND_AUTO_CREATE);
        return true;
    }

    boolean disconnectLocalRouter(String domain) {
        if (TextUtils.isEmpty(domain)) {
            return false;
        } else if (PROCESS_NAME.equals(domain)) {
            stopSelf();
            return true;
        } else if (null == mLocalRouterConnectionMap.get(domain)) {
            return false;
        } else {
            mApplication.unbindService(mLocalRouterConnectionMap.get(domain));
            mLocalRouterAIDLMap.remove(domain);
            mLocalRouterConnectionMap.remove(domain);
            return true;
        }
    }

    /**
     */
    void stopSelf() {
        mIsStopping = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<String> locals = new ArrayList<>();
                locals.addAll(mLocalRouterAIDLMap.keySet());
                for (String domain : locals) {
                    ILocalRouterAIDL aidl = mLocalRouterAIDLMap.get(domain);
                    if (null != aidl) {
                        try {
                            aidl.stopWideRouter();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        disconnectLocalRouter(domain);
                    }
                }
                try {
                    Thread.sleep(1000);
                    mApplication.stopService(new Intent(mApplication, WideRouterConnectService.class));
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.exit(0);
            }
        }).start();
    }

    @Deprecated
    boolean answerLocalAsync(@NonNull RouterRequest routerRequest) {
        ILocalRouterAIDL target = mLocalRouterAIDLMap.get(routerRequest.getDomain());
        if (target == null) {
            Class<? extends LocalRouterConnectService> clazz = sLocalRouterClasses.get(routerRequest.getDomain()).targetClass;
            if (null == clazz) {
                return false;
            } else {
                return true;
            }
        } else {
            try {
                return target.checkResponseAsync(routerRequest.toString());
            } catch (RemoteException e) {
                e.printStackTrace();
                return true;
            }
        }
    }

    public RouterResponse route(RouterRequest routerRequest) {
        Logger.d(TAG, "Process:" + PROCESS_NAME + "\nWide route start: " + System.currentTimeMillis() + "\nRequest:" + routerRequest.toString());
        RouterResponse routerResponse = new RouterResponse();
        if (mIsStopping) {
            ErrorAction defaultNotFoundAction = new ErrorAction(true, MaActionResult.CODE_WIDE_STOPPING, "Wide router is stopping.");
            MaActionResult result = defaultNotFoundAction.invoke(mApplication, routerRequest.getData());
            routerResponse.mIsAsync = true;
            routerResponse.mResultString = result.toString();
            return routerResponse;
        }
        if (PROCESS_NAME.equals(routerRequest.getDomain())) {
            ErrorAction defaultNotFoundAction = new ErrorAction(true, MaActionResult.CODE_TARGET_IS_WIDE, "Domain can not be " + PROCESS_NAME + ".");
            MaActionResult result = defaultNotFoundAction.invoke(mApplication, routerRequest.getData());
            routerResponse.mIsAsync = true;
            routerResponse.mResultString = result.toString();
            return routerResponse;
        }
        ILocalRouterAIDL target = mLocalRouterAIDLMap.get(routerRequest.getDomain());
        if (null == target) {
            if (!connectLocalRouter(routerRequest.getDomain())) {
                ErrorAction defaultNotFoundAction = new ErrorAction(false, MaActionResult.CODE_ROUTER_NOT_REGISTER, "The " + routerRequest.getDomain() + " has not registered.");
                MaActionResult result = defaultNotFoundAction.invoke(mApplication, routerRequest.getData());
                routerResponse.mIsAsync = false;
                routerResponse.mResultString = result.toString();
                Logger.d(TAG, "Process:" + PROCESS_NAME + "\nLocal not register end: " + System.currentTimeMillis() + "\nRequest:" + routerRequest.toString());
                return routerResponse;
            } else {
                // Wait to bind the target process connect service, timeout is 30s.
                Logger.d(TAG, "Process:" + PROCESS_NAME + "\nBind local router start: " + System.currentTimeMillis() + "\nRequest:" + routerRequest.toString());
                int time = 0;
                while (true) {
                    target = mLocalRouterAIDLMap.get(routerRequest.getDomain());
                    if (null == target) {
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        time++;
                    } else {
                        Logger.d(TAG, "Process:" + PROCESS_NAME + "\nBind local router end: " + System.currentTimeMillis() + "\nRequest:" + routerRequest.toString());
                        break;
                    }
                    if (time >= 600) {
                        ErrorAction defaultNotFoundAction = new ErrorAction(true, MaActionResult.CODE_CANNOT_BIND_LOCAL, "Can not bind " + routerRequest.getDomain());
                        MaActionResult result = defaultNotFoundAction.invoke(mApplication, routerRequest.getData());
                        routerResponse.mIsAsync = true;
                        routerResponse.mResultString = result.toString();
                        return routerResponse;
                    }
                }
            }
        }
        try {
            String resultString = target.route(routerRequest.toString());
            routerResponse.mIsAsync = true;
            routerResponse.mResultString = resultString;
            Logger.d(TAG, "Process:" + PROCESS_NAME + "\nWide route end: " + System.currentTimeMillis() + "\nRequest:" + routerRequest.toString());
        } catch (RemoteException e) {
            e.printStackTrace();
            ErrorAction defaultNotFoundAction = new ErrorAction(true, MaActionResult.CODE_REMOTE_EXCEPTION, e.getMessage());
            MaActionResult result = defaultNotFoundAction.invoke(mApplication, routerRequest.getData());
            routerResponse.mIsAsync = true;
            routerResponse.mResultString = result.toString();
            return routerResponse;

        }
        return routerResponse;
    }

}
