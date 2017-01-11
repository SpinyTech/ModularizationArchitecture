package com.spinytech.macore.router;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import com.spinytech.macore.ILocalRouterAIDL;
import com.spinytech.macore.MaActionResult;
import com.spinytech.macore.MaApplication;

/**
 * Created by wanglei on 2016/11/29.
 */

public class LocalRouterConnectService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return stub;
    }

    ILocalRouterAIDL.Stub stub = new ILocalRouterAIDL.Stub() {

        @Override
        public boolean checkResponseAsync(String routerRequest) throws RemoteException {
            return LocalRouter.getInstance((MaApplication) getApplication()).
                    answerWiderAsync(new RouterRequest
                            .Builder(getApplicationContext())
                            .json(routerRequest)
                            .build());
        }

        @Override
        public String route(String routerRequest) {
            try {
                return LocalRouter
                        .getInstance((MaApplication) getApplication())
                        .route(LocalRouterConnectService.this, new RouterRequest
                                .Builder(getApplicationContext())
                                .json(routerRequest)
                                .build())
                        .get();
            } catch (Exception e) {
                e.printStackTrace();
                return new MaActionResult.Builder().msg(e.getMessage()).build().toString();
            }
        }

        @Override
        public boolean stopWideRouter() throws RemoteException {
            LocalRouter
                    .getInstance((MaApplication) getApplication())
                    .disconnectWideRouter();
            return true;
        }
    };
}
