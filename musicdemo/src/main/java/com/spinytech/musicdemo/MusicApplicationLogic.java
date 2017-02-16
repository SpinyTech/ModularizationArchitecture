package com.spinytech.musicdemo;

import android.widget.Toast;

import com.linked.annotion.Module;
import com.spinytech.macore.multiprocess.BaseApplicationLogic;
import com.spinytech.macore.router.LocalRouter;

/**
 * Created by wanglei on 2016/11/30.
 */
@Module(name = "music")
public class MusicApplicationLogic extends BaseApplicationLogic {
    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(mApplication, "MusicApplicationLogic onCreate", Toast.LENGTH_SHORT).show();
    }
}
