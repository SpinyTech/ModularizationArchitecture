package com.spinytech.musicdemo;

import com.linked.annotion.Provider;
import com.spinytech.macore.MaProvider;

/**
 * Created by wanglei on 2016/12/28.
 */
@Provider(processName = "com.spinytech.maindemo:music")
public class MusicProvider extends MaProvider{
    @Override
    protected String getName() {
        return "music";
    }
}
