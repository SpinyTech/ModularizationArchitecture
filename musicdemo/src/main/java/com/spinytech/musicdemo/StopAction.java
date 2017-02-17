package com.spinytech.musicdemo;

import android.content.Context;
import android.content.Intent;

import com.linked.annotion.Action;
import com.spinytech.macore.MaAction;
import com.spinytech.macore.router.MaActionResult;
import com.spinytech.macore.router.RouterRequest;
import com.spinytech.macore.tools.Logger;

/**
 * Created by wanglei on 2016/12/28.
 */
@Action(processName = "com.spinytech.maindemo:music", providerName = "music")
public class StopAction implements MaAction {

    @Override
    public boolean isAsync(Context context, RouterRequest requestData) {
        return false;
    }

    @Override
    public MaActionResult invoke(Context context, RouterRequest requestData) {
        Intent intent = new Intent(context, MusicService.class);
        intent.putExtra("command", "stop");
        context.startService(intent);
        MaActionResult result = new MaActionResult.Builder()
                .code(MaActionResult.CODE_SUCCESS)
                .msg("stop success")
                .data("")
                .result(null)
                .build();

        Logger.d("StopAction", "\nStopAction end: " + System.currentTimeMillis());
        return result;
    }

    @Override
    public String getName() {
        return "stop";
    }
}
