package com.spinytech.webdemo;

import android.widget.Toast;

import com.linked.annotion.Module;
import com.spinytech.macore.multiprocess.BaseApplicationLogic;

/**
 * Created by wanglei on 2017/1/4.
 */
@Module(name = "web")
public class WebApplicationLogic extends BaseApplicationLogic {

    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(mApplication, "WebApplicationLogic onCreate", Toast.LENGTH_SHORT).show();
    }
}
