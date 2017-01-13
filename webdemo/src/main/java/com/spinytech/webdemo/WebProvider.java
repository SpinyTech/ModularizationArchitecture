package com.spinytech.webdemo;

import com.spinytech.macore.MaProvider;

/**
 * Created by wanglei on 2017/1/4.
 */

public class WebProvider extends MaProvider{
    @Override
    protected void registerActions() {
        registerAction("web",new WebAction());
    }
}
