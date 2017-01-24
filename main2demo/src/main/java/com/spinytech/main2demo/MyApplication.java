package com.spinytech.main2demo;

import com.spinytech.macore.MaApplication;
import com.spinytech.macore.router.WideRouter;

/**
 * Created by wanglei on 2016/11/29.
 */

public class MyApplication extends MaApplication {
    @Override
    public void initializeAllProcessRouter() {
        WideRouter.registerLocalRouter("com.spinytech.main2demo",MainRouterConnectService.class);
    }

    @Override
    protected void initializeLogic() {
        registerApplicationLogic("com.spinytech.main2demo",999, MainApplicationLogic.class);
    }

    @Override
    public boolean needMultipleProcess() {
        return true;
    }
}
