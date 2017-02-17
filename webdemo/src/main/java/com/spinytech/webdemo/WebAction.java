package com.spinytech.webdemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.linked.annotion.Action;
import com.spinytech.macore.MaAction;
import com.spinytech.macore.router.MaActionResult;
import com.spinytech.macore.router.RouterRequest;

/**
 * Created by wanglei on 2017/1/4.
 */
@Action(processName = "com.spinytech.maindemo", providerName = "web")
public class WebAction implements MaAction {
    @Override
    public boolean isAsync(Context context, RouterRequest requestData) {
        return false;
    }

    @Override
    public MaActionResult invoke(Context context, RouterRequest requestData) {
        if(context instanceof Activity){
            Intent i = new Intent(context, WebActivity.class);
            context.startActivity(i);
        }else{
            Intent i = new Intent(context, WebActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
        return new MaActionResult.Builder().code(MaActionResult.CODE_SUCCESS).msg("success").data("").build();
    }

    @Override
    public String getName() {
        return "web";
    }
}
