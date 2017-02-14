package com.spinytech.maindemo;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.spinytech.macore.MaAction;
import com.spinytech.macore.MaActionResult;

import java.util.HashMap;

/**
 * Created by wanglei on 2016/12/28.
 */

public class SyncAction extends MaAction {

    @Override
    public boolean isAsync(Context context, HashMap<String, String> requestData) {
        return false;
    }

    @Override
    public MaActionResult invoke(Context context, HashMap<String, String> requestData) {
        String temp = "";
        if(!TextUtils.isEmpty(requestData.get("1"))){
            temp+=requestData.get("1");
        }
        if(!TextUtils.isEmpty(requestData.get("2"))){
            temp+=requestData.get("2");
        }
        Toast.makeText(context, "SyncAction.invoke:"+temp, Toast.LENGTH_SHORT).show();
        MaActionResult result = new MaActionResult.Builder()
                .code(MaActionResult.CODE_SUCCESS)
                .msg("success")
                .data(temp)
                .result(null)
                .build();
        return result;
    }
}
