package com.spinytech.picdemo;

import com.spinytech.macore.MaProvider;

/**
 * Created by wanglei on 2017/1/4.
 */

public class PicProvider extends MaProvider{
    @Override
    protected void registerActions() {
        registerAction("pic",new PicAction());
    }
}
