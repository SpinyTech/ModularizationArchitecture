package com.spinytech.maindemo;

import com.spinytech.macore.MaApplication;
import com.spinytech.macore.router.WideRouter;
import com.spinytech.musicdemo.MusicApplicationLogic;
import com.spinytech.musicdemo.MusicRouterConnectService;
import com.spinytech.picdemo.PicApplicationLogic;
import com.spinytech.picdemo.PicRouterConnectService;
import com.spinytech.webdemo.WebApplicationLogic;

/**
 * Created by wanglei on 2016/11/29.
 */

public class MyApplication extends MaApplication {
    @Override
    public void initializeAllProcessRouter() {
        WideRouter.registerLocalRouter("com.spinytech.maindemo",MainRouterConnectService.class);
        WideRouter.registerLocalRouter("com.spinytech.maindemo:music",MusicRouterConnectService.class);
        WideRouter.registerLocalRouter("com.spinytech.maindemo:pic",PicRouterConnectService.class);
    }

    @Override
    protected void initializeLogic() {
        registerApplicationLogic("com.spinytech.maindemo",999, MainApplicationLogic.class);
        registerApplicationLogic("com.spinytech.maindemo",998, WebApplicationLogic.class);
        registerApplicationLogic("com.spinytech.maindemo:music",999, MusicApplicationLogic.class);
        registerApplicationLogic("com.spinytech.maindemo:pic",999, PicApplicationLogic.class);
    }

    @Override
    public boolean needMultipleProcess() {
        return true;
    }
}
