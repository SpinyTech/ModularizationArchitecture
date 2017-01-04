package com.spinytech.macore.router;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.spinytech.macore.ErrorAction;
import com.spinytech.macore.ILocalRouterAIDL;
import com.spinytech.macore.MaActionResult;
import com.spinytech.macore.MaApplication;
import com.spinytech.macore.tools.ProcessUtil;

import java.util.HashMap;

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

    public static void registerLocalRouter(String processName, Class<? extends LocalRouterConnectService> targetClass, boolean needAutoConnect) {
        if (null == sLocalRouterClasses) {
            sLocalRouterClasses = new HashMap<>();
        }
        ConnectServiceWrapper connectServiceWrapper = new ConnectServiceWrapper(needAutoConnect, targetClass);
        sLocalRouterClasses.put(processName, connectServiceWrapper);
    }

    protected boolean connectLocalRouter(final String domain) {
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
        Log.e(TAG, PROCESS_NAME + ", start: " + System.currentTimeMillis() + "\n" + routerRequest.toString());
        RouterResponse routerResponse = new RouterResponse();
        if (PROCESS_NAME.equals(routerRequest.getDomain())) {

        }
        ILocalRouterAIDL target = mLocalRouterAIDLMap.get(routerRequest.getDomain());
        if (null == target) {
            if (!connectLocalRouter(routerRequest.getDomain())) {
                ErrorAction defaultNotFoundAction = new ErrorAction(false, MaActionResult.CODE_ROUTER_NOT_REGISTER, "The " + routerRequest.getDomain() + " has not registered.");
                MaActionResult result = defaultNotFoundAction.invoke(mApplication, routerRequest.getData());
                routerResponse.mIsAsync = false;
                routerResponse.mResultString = result.toString();
                Log.e(TAG, PROCESS_NAME + ", no register end: " + System.currentTimeMillis() + "\n" + routerRequest.toString());
                return routerResponse;
            } else {
                // Wait to bind the target process connect service, timeout is 30s.
                Log.e(TAG, PROCESS_NAME + ", bind start: " + System.currentTimeMillis() + "\n" + routerRequest.toString());
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
                        Log.e(TAG, PROCESS_NAME + ", bind end: " + System.currentTimeMillis() + "\n" + routerRequest.toString());
                        break;
                    }
                    if (time >= 600) {
                        ErrorAction defaultNotFoundAction = new ErrorAction(true, MaActionResult.CODE_CANNOT_BIND_TARGET, "Can not bind " + routerRequest.getDomain());
                        MaActionResult result = defaultNotFoundAction.invoke(mApplication, routerRequest.getData());
                        routerResponse.mIsAsync = true;
                        routerResponse.mResultString = result.toString();
                        Log.e(TAG, PROCESS_NAME + ", time out end: " + System.currentTimeMillis() + "\n" + routerRequest.toString());
                        return routerResponse;
                    }
                }
            }
        }
        try {
            String resultString = target.route(routerRequest.toString());
            routerResponse.mIsAsync = target.checkResponseAsync(routerRequest.toString());
            routerResponse.mResultString = resultString;
            Log.e(TAG, PROCESS_NAME + ", end: " + System.currentTimeMillis() + "\n" + routerRequest.toString());
        } catch (RemoteException e) {
            e.printStackTrace();
            ErrorAction defaultNotFoundAction = new ErrorAction(true, MaActionResult.CODE_REMOTE_EXCEPTION, e.getMessage());
            MaActionResult result = defaultNotFoundAction.invoke(mApplication, routerRequest.getData());
            routerResponse.mIsAsync = true;
            routerResponse.mResultString = result.toString();
            Log.e(TAG, PROCESS_NAME + ", error end: " + System.currentTimeMillis() + "\n" + routerRequest.toString());
            return routerResponse;

        }
        return routerResponse;
    }

    boolean shutdownRouter(String domain) {
        if (TextUtils.isEmpty(domain)) {
            return false;
        }
        else if (PROCESS_NAME.equals(domain)) {
            //TODO
            return false;
        } else if (null == mLocalRouterConnectionMap.get(domain)) {
            return false;
        } else {
            mApplication.unbindService(mLocalRouterConnectionMap.get(domain));
            mLocalRouterAIDLMap.remove(domain);
            mLocalRouterConnectionMap.remove(domain);
            return true;
        }
    }
}
