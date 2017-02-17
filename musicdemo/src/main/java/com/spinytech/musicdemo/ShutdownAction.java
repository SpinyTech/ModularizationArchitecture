package com.spinytech.musicdemo;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.linked.annotion.Action;
import com.spinytech.macore.MaAction;
import com.spinytech.macore.router.MaActionResult;
import com.spinytech.macore.MaApplication;
import com.spinytech.macore.router.LocalRouter;
import com.spinytech.macore.router.RouterRequest;

/**
 * Created by wanglei on 2016/12/28.
 */
@Action(processName = "com.spinytech.maindemo:music", providerName = "music")
public class ShutdownAction implements MaAction {

    @Override
    public boolean isAsync(Context context, RouterRequest requestData) {
        return true;
    }

    @Override
    public MaActionResult invoke(Context context, RouterRequest requestData) {
        MaActionResult result = new MaActionResult.Builder()
                .code(MaActionResult.CODE_SUCCESS)
                .msg("success")
                .data("")
                .result(null)
                .build();
        context.getApplicationContext().stopService(new Intent(context,MusicService.class));

        boolean stopslef =LocalRouter.getInstance(MaApplication.getMaApplication()).stopSelf(MusicRouterConnectService.class);
        Log.e("stopslef",""+stopslef);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.exit(0);
            }
        }).start();
        return result;
    }

    @Override
    public String getName() {
        return "shutdown";
    }
}
