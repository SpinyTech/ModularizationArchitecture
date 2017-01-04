package com.spinytech.macore.router;

import android.content.Context;

import com.google.gson.Gson;
import com.spinytech.macore.tools.ProcessUtil;

import java.util.HashMap;

/**
 * Created by wanglei on 2016/12/27.
 */

public class RouterRequest {

    private String from;
    private String domain;
    private String provider;
    private String action;
    private HashMap<String, String> data;


    private RouterRequest(Builder builder) {
        this.from = builder.mFrom;
        this.domain = builder.mDomain;
        this.provider = builder.mProvider;
        this.action = builder.mAction;
        this.data = builder.mData;
    }

    public String getFrom() {
        return from;
    }

    public String getDomain() {
        return domain;
    }

    public String getProvider() {
        return provider;
    }

    public String getAction() {
        return action;
    }

    public HashMap<String, String> getData() {
        return data;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public static class Builder {
        private String mFrom;
        private String mDomain;
        private String mProvider;
        private String mAction;
        private HashMap<String, String> mData;

        public Builder(Context context) {
            mFrom = ProcessUtil.getProcessName(context, ProcessUtil.getMyProcessId());
            mDomain = ProcessUtil.getProcessName(context, ProcessUtil.getMyProcessId());
            mProvider = "";
            mAction = "";
            mData = new HashMap<>();
        }

        public Builder requestString(String requestString) {
            RouterRequest routerRequest = new Gson().fromJson(requestString, RouterRequest.class);
            this.mFrom = routerRequest.from;
            this.mDomain = routerRequest.domain;
            this.mProvider = routerRequest.provider;
            this.mAction = routerRequest.action;
            this.mData = routerRequest.data;
            return this;
        }

        public Builder domain(String domain) {
            this.mDomain = domain;
            return this;
        }


        public Builder provider(String provider) {
            this.mProvider = provider;
            return this;
        }


        public Builder action(String action) {
            this.mAction = action;
            return this;
        }


        public Builder data(String key, String data) {
            this.mData.put(key, data);
            return this;
        }

        public RouterRequest build() {
            return new RouterRequest(this);
        }
    }

}
