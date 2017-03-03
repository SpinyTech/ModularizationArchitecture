package com.spinytech.macore.router;

import org.json.JSONException;
import org.json.JSONObject;

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

    /**
     *  This field is MaActionResult.toString()
     */
    String mResultString;

    Future<String> mAsyncResponse;

    public RouterResponse() {
        this(TIME_OUT);
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
            parseResult();
        }else{
            parseResult();
        }
        return mResultString;
    }

    private void parseResult(){
        if (!mHasGet) {
            try {
                JSONObject jsonObject = new JSONObject(mResultString);
                this.mCode = jsonObject.getInt("code");
                this.mMessage = jsonObject.getString("msg");
                this.mData = jsonObject.getString("data");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mHasGet = true;
        }
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

}
