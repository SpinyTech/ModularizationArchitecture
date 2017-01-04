package com.spinytech.macore.router;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.spinytech.macore.IWideRouterAIDL;
import com.spinytech.macore.MaActionResult;
import com.spinytech.macore.MaApplication;

/**
 * Created by wanglei on 2016/11/29.
 */

public final class WideRouterConnectService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        if (!(getApplication() instanceof MaApplication)) {
            throw new RuntimeException("Please check your AndroidManifest.xml and make sure the application is instance of MaApplication.");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("WRCS","onDestroy");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return stub;
    }

    IWideRouterAIDL.Stub stub = new IWideRouterAIDL.Stub() {

        @Override
        public boolean checkResponseAsync(String routerRequest) throws RemoteException {
            return WideRouter
                    .getInstance((MaApplication) getApplication())
                    .answerLocalAsync(new RouterRequest
                            .Builder(getApplicationContext())
                            .requestString(routerRequest)
                            .build());
        }

        @Override
        public String route(String routerRequest) {
            try {
                return WideRouter
                        .getInstance((MaApplication) getApplication())
                        .route(new RouterRequest
                                .Builder(getApplicationContext())
                                .requestString(routerRequest)
                                .build())
                        .get();
            } catch (Exception e) {
                e.printStackTrace();
                return new MaActionResult.Builder()
                        .code(MaActionResult.CODE_ERROR)
                        .msg(e.getMessage())
                        .build()
                        .toString();
            }
        }

        @Override
        public boolean shutdownRouter(String domain) throws RemoteException {
            return WideRouter
                    .getInstance((MaApplication) getApplication())
                    .shutdownRouter(domain);
        }
    };
}
