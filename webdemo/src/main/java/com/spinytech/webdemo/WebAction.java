package com.spinytech.webdemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.spinytech.macore.MaAction;
import com.spinytech.macore.MaActionResult;

import java.util.HashMap;

/**
 * Created by wanglei on 2017/1/4.
 */

public class WebAction extends MaAction {
    @Override
    public boolean isAsync(Context context, HashMap<String, String> requestData) {
        return false;
    }

    @Override
    public MaActionResult invoke(Context context, HashMap<String, String> requestData) {
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
}
