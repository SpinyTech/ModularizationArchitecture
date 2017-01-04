package com.spinytech.maindemo;

import com.spinytech.macore.MaProvider;

/**
 * Created by wanglei on 2016/12/28.
 */

public class MainProvider extends MaProvider {
    @Override
    protected void registerActions() {
        registerAction("sync",new SyncAction());
        registerAction("async",new AsyncAction());
    }
}
