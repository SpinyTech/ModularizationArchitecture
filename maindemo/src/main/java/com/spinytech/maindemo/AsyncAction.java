package com.spinytech.maindemo;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.linked.annotion.Action;
import com.spinytech.macore.MaAction;
import com.spinytech.macore.router.MaActionResult;
import com.spinytech.macore.router.RouterRequest;

/**
 * Created by wanglei on 2016/12/28.
 */
@Action(processName = "com.spinytech.maindemo", providerName = "main")
public class AsyncAction implements MaAction {

    @Override
    public boolean isAsync(Context context, RouterRequest requestData) {
        return true;
    }

    @Override
    public MaActionResult invoke(Context context, RouterRequest requestData) {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String temp = "";
        if (!TextUtils.isEmpty((String) requestData.getData().get("1"))) {
            temp += requestData.getData().get("1");
        }
        if (!TextUtils.isEmpty((CharSequence) requestData.getData().get("2"))) {
            temp += requestData.getData().get("2");
        }
        Log.e("AsyncAction", temp);
        MaActionResult result = new MaActionResult.Builder()
                .code(MaActionResult.CODE_SUCCESS)
                .msg("success")
                .data(temp)
                .result(null)
                .build();
        return result;
    }

    @Override
    public String getName() {
        return "async";
    }
}
