package com.spinytech.macore.router;

import android.content.Context;
import android.text.TextUtils;

import com.spinytech.macore.tools.Logger;
import com.spinytech.macore.tools.ProcessUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by erfli on 2/17/17.
 */

public class RouterRequestUtil {
    static final String TAG = "RouterRequest";
    static final int length = 64;
    public static volatile RouterRequest[] table = new RouterRequest[length];
    static final int RESET_NUM = 1000;
    static volatile String DEFAULT_PROCESS = "";

    static {
        for (int i = 0; i < RouterRequestUtil.length; i++) {
            RouterRequestUtil.table[i] = new RouterRequest();
        }
    }

    public static RouterRequest json(String requestJsonString) {
        RouterRequest routerRequest = new RouterRequest();
        try {
            JSONObject jsonObject = new JSONObject(requestJsonString);
            routerRequest.from = jsonObject.getString("from");
            routerRequest.domain = jsonObject.getString("domain");
            routerRequest.provider = jsonObject.getString("provider");
            routerRequest.action = jsonObject.getString("action");
            try {
                JSONObject jsonData = new JSONObject(jsonObject.getString("data"));
                Iterator it = jsonData.keys();
                while (it.hasNext()) {
                    String key = String.valueOf(it.next());
                    String value = (String) jsonData.get(key);
                    routerRequest.data.put(key, value);
                }
            } catch (Exception e) {
                e.printStackTrace();
                routerRequest.data = new HashMap<>();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return routerRequest;
    }

    public static RouterRequest url(String url) {
        RouterRequest routerRequest = new RouterRequest();
        int questIndex = url.indexOf('?');
        String[] urls = url.split("\\?");
        if (urls.length != 1 && urls.length != 2) {
            Logger.e(TAG, "The url is illegal.");
            return routerRequest;
        }
        String[] targets = urls[0].split("/");
        if (targets.length == 3) {
            routerRequest.domain = targets[0];
            routerRequest.provider = targets[1];
            routerRequest.action = targets[2];
        } else {
            Logger.e(TAG, "The url is illegal.");
            return routerRequest;
        }
        //Add params
        if (questIndex != -1) {
            String queryString = urls[1];
            if (queryString != null && queryString.length() > 0) {
                int ampersandIndex, lastAmpersandIndex = 0;
                String subStr, key, value;
                String[] paramPair, values, newValues;
                do {
                    ampersandIndex = queryString.indexOf('&', lastAmpersandIndex) + 1;
                    if (ampersandIndex > 0) {
                        subStr = queryString.substring(lastAmpersandIndex, ampersandIndex - 1);
                        lastAmpersandIndex = ampersandIndex;
                    } else {
                        subStr = queryString.substring(lastAmpersandIndex);
                    }
                    paramPair = subStr.split("=");
                    key = paramPair[0];
                    value = paramPair.length == 1 ? "" : paramPair[1];
                    try {
                        value = URLDecoder.decode(value, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    routerRequest.data.put(key, value);
                } while (ampersandIndex > 0);
            }
        }
        return routerRequest;
    }

    public static String getProcess(Context context) {
        if (TextUtils.isEmpty(DEFAULT_PROCESS) || ProcessUtil.UNKNOWN_PROCESS_NAME.equals(DEFAULT_PROCESS)) {
            DEFAULT_PROCESS = ProcessUtil.getProcessName(context, ProcessUtil.getMyProcessId());
        }
        return DEFAULT_PROCESS;
    }

    public static RouterRequest obtain(Context context) {
        return obtain(context, 0);
    }

    public static RouterRequest obtain(Context context, int retryTime) {
        int index = RouterRequest.sIndex.getAndIncrement();
        if (index > RESET_NUM) {
            RouterRequest.sIndex.compareAndSet(index, 0);
            if (index > RESET_NUM * 2) {
                RouterRequest.sIndex.set(0);
            }
        }

        int num = index & (length - 1);

        RouterRequest target = table[num];

        if (target.isIdle.compareAndSet(true, false)) {
            target.from = getProcess(context);
            target.domain = getProcess(context);
            target.provider = "";
            target.action = "";
            target.data.clear();
            return target;
        } else {
            if (retryTime < 5) {
                return obtain(context, retryTime++);
            } else {
                return new RouterRequest(context);
            }

        }
    }
}
