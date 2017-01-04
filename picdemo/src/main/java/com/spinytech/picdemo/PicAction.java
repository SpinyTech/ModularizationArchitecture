package com.spinytech.picdemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.spinytech.macore.MaAction;
import com.spinytech.macore.MaActionResult;

import java.util.HashMap;

/**
 * Created by wanglei on 2017/1/4.
 */

public class PicAction extends MaAction {
    @Override
    public boolean isAsync(Context context, HashMap<String, String> requestData) {
        return false;
    }

    @Override
    public MaActionResult invoke(Context context, HashMap<String, String> requestData) {
        String isBigString = requestData.get("is_big");
        boolean isBig = "1".equals(isBigString);
        if(context instanceof Activity){
            Intent i = new Intent(context, PicActivity.class);
            i.putExtra("is_big",isBig);
            context.startActivity(i);
        }else{
            Intent i = new Intent(context, PicActivity.class);
            i.putExtra("is_big",isBig);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
        return new MaActionResult.Builder().code(MaActionResult.CODE_SUCCESS).msg("success").data("").build();
    }
}
