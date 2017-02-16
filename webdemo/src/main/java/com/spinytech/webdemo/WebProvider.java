package com.spinytech.webdemo;

import com.linked.annotion.Provider;
import com.spinytech.macore.MaProvider;

/**
 * Created by wanglei on 2017/1/4.
 */
@Provider(processName = "com.spinytech.maindemo")
public class WebProvider extends MaProvider{
    @Override
    protected String getName() {
        return "web";
    }
}
