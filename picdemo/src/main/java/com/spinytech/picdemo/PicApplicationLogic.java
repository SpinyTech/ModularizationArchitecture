package com.spinytech.picdemo;

import android.widget.Toast;

import com.linked.annotion.Module;
import com.spinytech.macore.multiprocess.BaseApplicationLogic;
import com.spinytech.macore.router.LocalRouter;

/**
 * Created by wanglei on 2017/1/4.
 */
@Module(name = "pic")
public class PicApplicationLogic extends BaseApplicationLogic {

    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(mApplication, "Pic_Process on Create", Toast.LENGTH_SHORT).show();
    }
}
