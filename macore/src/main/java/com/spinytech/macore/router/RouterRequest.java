package com.spinytech.macore.router;

import android.content.Context;

import com.google.gson.Gson;
import com.spinytech.macore.tools.Logger;
import com.spinytech.macore.tools.ProcessUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;

/**
 * Created by wanglei on 2016/12/27.
 */

public class RouterRequest {
    private static final String TAG = "RouterRequest";
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

        public Builder json(String requestJsonString) {
            RouterRequest routerRequest = new Gson().fromJson(requestJsonString, RouterRequest.class);
            this.mFrom = routerRequest.from;
            this.mDomain = routerRequest.domain;
            this.mProvider = routerRequest.provider;
            this.mAction = routerRequest.action;
            this.mData = routerRequest.data;
            return this;
        }

        public Builder url(String url){
            int questIndex = url.indexOf('?');
            String[] urls = url.split("\\?");
            if(urls.length!=1||urls.length!=2){
                Logger.e(TAG,"The url is illegal.");
                return this;
            }
            String[] targets = urls[0].split("/");
            if(targets.length==3){
                this.mDomain = targets[0];
                this.mProvider = targets[1];
                this.mAction = targets[2];
            }else{
                Logger.e(TAG,"The url is illegal.");
                return this;
            }
            //Add params
            if (questIndex != -1){
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
                        mData.put(key, value);
                    } while (ampersandIndex > 0);
                }
            }
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
