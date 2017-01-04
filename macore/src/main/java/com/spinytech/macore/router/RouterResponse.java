package com.spinytech.macore.router;

import com.google.gson.Gson;
import com.spinytech.macore.MaActionResult;

import java.lang.reflect.Type;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by wanglei on 2016/12/27.
 */

public class RouterResponse {

    private static final int TIME_OUT = 30 * 1000;

    private long mTimeOut = 0;
    private boolean mHasGet = false;
    boolean mIsAsync = true;

    int mCode = -1;

    String mMessage = "";

    String mData;

    Object mObject;

    // This is MaActionResult.toString()
    String mResultString;

    Future<String> mAsyncResponse;

    public RouterResponse() {
        mTimeOut = TIME_OUT;
    }

    public RouterResponse(long timeout) {
        if (timeout > TIME_OUT * 2 || timeout < 0) {
            timeout = TIME_OUT;
        }
        mTimeOut = timeout;
    }

    public boolean isAsync() {
        return mIsAsync;
    }

    public String get() throws Exception {
        if (mIsAsync) {
            mResultString = mAsyncResponse.get(mTimeOut, TimeUnit.MILLISECONDS);
            if (!mHasGet) {
                MaActionResult result = new Gson().fromJson(mResultString, MaActionResult.class);
                this.mCode = result.getCode();
                this.mMessage = result.getMsg();
                this.mData = result.getData();
                mHasGet = true;
            }
        }
        return mResultString;
    }

    public int getCode() throws Exception {
        if (!mHasGet) {
            get();
        }
        return mCode;
    }

    public String getMessage() throws Exception {
        if (!mHasGet) {
            get();
        }
        return mMessage;
    }

    public String getData() throws Exception {
        if (!mHasGet) {
            get();
        }
        return mData;
    }

    public Object getObject() throws Exception {
        if (!mHasGet) {
            get();
        }
        return mObject;
    }

    public <T> T getDataEntity(Type type) throws Exception {
        if (!mHasGet) {
            get();
        }
        Gson gson = new Gson();
        return gson.fromJson(get(), type);
    }

    public <T> T getDataEntity(Class<T> clazz) throws Exception {
        if (!mHasGet) {
            get();
        }
        Gson gson = new Gson();
        return gson.fromJson(get(), clazz);
    }

}
