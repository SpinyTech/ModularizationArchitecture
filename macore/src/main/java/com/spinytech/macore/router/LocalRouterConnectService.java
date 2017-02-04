package com.spinytech.macore.router;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

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
        Log.e("MRCS","onBind");
        return stub;
    }

    ILocalRouterAIDL.Stub stub = new ILocalRouterAIDL.Stub() {

        @Override
        public boolean checkResponseAsync(String routerRequest) throws RemoteException {
            return LocalRouter.getInstance(MaApplication.getMaApplication()).
                    answerWiderAsync(new RouterRequest
                            .Builder(getApplicationContext())
                            .json(routerRequest)
                            .build());
        }

        @Override
        public String route(String routerRequest) {
            try {
                LocalRouter localRouter = LocalRouter.getInstance(MaApplication.getMaApplication());
                RouterRequest routerRequest1 = new RouterRequest
                        .Builder(getApplicationContext())
                        .json(routerRequest)
                        .build();
                RouterResponse routerResponse = localRouter.route(LocalRouterConnectService.this,routerRequest1);
                return routerResponse.get();
            } catch (Exception e) {
                e.printStackTrace();
                return new MaActionResult.Builder().msg(e.getMessage()).build().toString();
            }
        }

        @Override
        public boolean stopWideRouter() throws RemoteException {
            LocalRouter
                    .getInstance(MaApplication.getMaApplication())
                    .disconnectWideRouter();
            return true;
        }

        @Override
        public void connectWideRouter() throws RemoteException {
            LocalRouter
                    .getInstance(MaApplication.getMaApplication())
                    .connectWideRouter();
        }
    };
}
