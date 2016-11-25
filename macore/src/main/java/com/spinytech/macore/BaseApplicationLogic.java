package com.spinytech.macore;

import android.app.Application;
import android.content.res.Configuration;
import android.support.annotation.NonNull;

/**
 * Created by wanglei on 2016/11/25.
 */

public class BaseApplicationLogic {
    protected Application mApplication;

    public BaseApplicationLogic() {
    }

    public void setApplication(@NonNull Application application) {
        mApplication = application;
    }

    public void onCreate() {
    }

    public void onTerminate() {
    }

    public void onLowMemory() {
    }

    public void onTrimMemory(int level) {
    }

    public void onConfigurationChanged(Configuration newConfig) {
    }
}
