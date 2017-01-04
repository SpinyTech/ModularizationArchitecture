package com.spinytech.musicdemo;

import com.spinytech.macore.MaProvider;

/**
 * Created by wanglei on 2016/12/28.
 */

public class MusicProvider extends MaProvider{
    @Override
    protected void registerActions() {
        registerAction("play",new PlayAction());
        registerAction("stop",new StopAction());
        registerAction("shutdown",new ShutdownAction());
    }
}
