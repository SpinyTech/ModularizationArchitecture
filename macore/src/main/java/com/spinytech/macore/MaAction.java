package com.spinytech.macore;

import android.content.Context;

import java.util.HashMap;

/**
 * Created by wanglei on 2016/11/29.
 */

public abstract class MaAction {
    public abstract boolean isAsync(Context context, HashMap<String,String> requestData);
    public abstract MaActionResult invoke(Context context, HashMap<String,String> requestData);
}
