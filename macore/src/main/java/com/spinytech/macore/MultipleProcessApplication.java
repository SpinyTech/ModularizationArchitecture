package com.spinytech.macore;

import android.app.Application;
import android.content.res.Configuration;
import android.support.annotation.CallSuper;

import java.util.HashMap;

/**
 * Created by wanglei on 2016/11/25.
 */

public abstract class MultipleProcessApplication extends Application {
    protected BaseApplicationLogic mLogic;
    private HashMap<String, Class<? extends BaseApplicationLogic>> mLogicClassMap;

    @CallSuper
    @Override
    public void onCreate() {
        super.onCreate();
        mLogicClassMap = new HashMap<>();
        initLogic();
        dispatchLogic();
        if (null != mLogic) {
            mLogic.onCreate();
        }
    }

    protected abstract void initLogic();

    protected void dispatchLogic() {
        if (null != mLogicClassMap) {
            Class<? extends BaseApplicationLogic> logicClass = mLogicClassMap.get(ProcessUtil.getProcessName(this, ProcessUtil.getMyProcessId()));
            if (null != logicClass) {
                try {
                    mLogic = logicClass.newInstance();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            if (null != mLogic) {
                mLogic.setApplication(this);
            }
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (null != mLogic) {
            mLogic.onTerminate();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (null != mLogic) {
            mLogic.onLowMemory();
        }
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (null != mLogic) {
            mLogic.onTrimMemory(level);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (null != mLogic) {
            mLogic.onConfigurationChanged(newConfig);
        }
    }

}
