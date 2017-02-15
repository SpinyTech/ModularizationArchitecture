package com.spinytech.maindemo;

import android.content.Context;
import android.widget.TextView;
import android.widget.Toast;

import com.spinytech.macore.MaAction;
import com.spinytech.macore.MaActionResult;

import java.util.HashMap;

/**
 * Created by wanglei on 2017/2/15.
 */

public class AttachObjectAction extends MaAction {

    // The two following methods are invalid when the request has object attachment.
    @Override
    public boolean isAsync(Context context, HashMap<String, String> requestData) {
        return false;
    }

    @Override
    public MaActionResult invoke(Context context, HashMap<String, String> requestData) {
        return new MaActionResult.Builder().code(MaActionResult.CODE_NOT_IMPLEMENT).msg("This method has not yet been implemented.").build();
    }

    // We should override the following two methods when the request has a object attachment.
    @Override
    public boolean isAsync(Context context, HashMap<String, String> requestData, Object object) {
        return false;
    }

    @Override
    public MaActionResult invoke(Context context, HashMap<String, String> requestData, Object object) {
        if (object instanceof TextView) {
            ((TextView) object).setText("The text is changed by AttachObjectAction.");
            Toast returnToast = Toast.makeText(context, "I am returned Toast.", Toast.LENGTH_SHORT);
            return new MaActionResult.Builder().code(MaActionResult.CODE_SUCCESS).msg("success").object(returnToast).build();

        }
        return super.invoke(context, requestData, object);
    }
}
