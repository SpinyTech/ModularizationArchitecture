package com.spinytech.macore.router;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.text.TextUtils;

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

    boolean checkLocalRouterHasRegistered(final String domain) {
        ConnectServiceWrapper connectServiceWrapper = sLocalRouterClasses.get(domain);
        if(null == connectServiceWrapper){
            return false;
        }
        Class<? extends LocalRouterConnectService> clazz = connectServiceWrapper.targetClass;
        if (null == clazz) {
            return false;
        } else {
            return true;
        }
    }

    boolean connectLocalRouter(final String domain) {
        ConnectServiceWrapper connectServiceWrapper = sLocalRouterClasses.get(domain);
        if(null == connectServiceWrapper){
            return false;
        }
        Class<? extends LocalRouterConnectService> clazz = connectServiceWrapper.targetClass;
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
                    try {
                        mLocalRouterAIDL.connectWideRouter();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
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
            ILocalRouterAIDL aidl = mLocalRouterAIDLMap.get(domain);
            if (null != aidl) {
                try {
                    aidl.stopWideRouter();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
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
                        mApplication.unbindService(mLocalRouterConnectionMap.get(domain));
                        mLocalRouterAIDLMap.remove(domain);
                        mLocalRouterConnectionMap.remove(domain);
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

    boolean answerLocalAsync(String domain, String routerRequest) {
        ILocalRouterAIDL target = mLocalRouterAIDLMap.get(domain);
        if (target == null) {
            ConnectServiceWrapper connectServiceWrapper = sLocalRouterClasses.get(domain);
            if(null == connectServiceWrapper){
                return false;
            }
            Class<? extends LocalRouterConnectService> clazz = connectServiceWrapper.targetClass;
            if (null == clazz) {
                return false;
            } else {
                return true;
            }
        } else {
            try {
                return target.checkResponseAsync(routerRequest);
            } catch (RemoteException e) {
                e.printStackTrace();
                return true;
            }
        }
    }

    public RouterResponse route(String domain, String routerRequest) {
        Logger.d(TAG, "Process:" + PROCESS_NAME + "\nWide route start: " + System.currentTimeMillis());
        RouterResponse routerResponse = new RouterResponse();
        if (mIsStopping) {

            MaActionResult result = new MaActionResult.Builder()
                    .code(MaActionResult.CODE_WIDE_STOPPING)
                    .msg("Wide router is stopping.")
                    .build();
            routerResponse.mIsAsync = true;
            routerResponse.mResultString = result.toString();
            return routerResponse;
        }
        if (PROCESS_NAME.equals(domain)) {
            MaActionResult result = new MaActionResult.Builder()
                    .code(MaActionResult.CODE_TARGET_IS_WIDE)
                    .msg("Domain can not be " + PROCESS_NAME + ".")
                    .build();
            routerResponse.mIsAsync = true;
            routerResponse.mResultString = result.toString();
            return routerResponse;
        }
        ILocalRouterAIDL target = mLocalRouterAIDLMap.get(domain);
        if (null == target) {
            if (!connectLocalRouter(domain)) {
                MaActionResult result = new MaActionResult.Builder()
                        .code(MaActionResult.CODE_ROUTER_NOT_REGISTER)
                        .msg("The " + domain + " has not registered.")
                        .build();
                routerResponse.mIsAsync = false;
                routerResponse.mResultString = result.toString();
                Logger.d(TAG, "Process:" + PROCESS_NAME + "\nLocal not register end: " + System.currentTimeMillis());
                return routerResponse;
            } else {
                // Wait to bind the target process connect service, timeout is 30s.
                Logger.d(TAG, "Process:" + PROCESS_NAME + "\nBind local router start: " + System.currentTimeMillis());
                int time = 0;
                while (true) {
                    target = mLocalRouterAIDLMap.get(domain);
                    if (null == target) {
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        time++;
                    } else {
                        Logger.d(TAG, "Process:" + PROCESS_NAME + "\nBind local router end: " + System.currentTimeMillis());
                        break;
                    }
                    if (time >= 600) {
                        MaActionResult result = new MaActionResult.Builder()
                                .code(MaActionResult.CODE_CANNOT_BIND_LOCAL)
                                .msg("Can not bind " + domain + ", time out.")
                                .build();
                        routerResponse.mResultString = result.toString();
                        return routerResponse;
                    }
                }
            }
        }
        try {
            Logger.d(TAG, "Process:" + PROCESS_NAME + "\nWide target start: " + System.currentTimeMillis());
            String resultString = target.route(routerRequest);
            routerResponse.mResultString = resultString;
            Logger.d(TAG, "Process:" + PROCESS_NAME + "\nWide route end: " + System.currentTimeMillis());
        } catch (RemoteException e) {
            e.printStackTrace();
            MaActionResult result = new MaActionResult.Builder()
                    .code(MaActionResult.CODE_REMOTE_EXCEPTION)
                    .msg(e.getMessage())
                    .build();
            routerResponse.mResultString = result.toString();
            return routerResponse;
        }
        return routerResponse;
    }

}
