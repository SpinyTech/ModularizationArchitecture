package com.spinytech.picdemo;

import com.linked.annotion.Provider;
import com.spinytech.macore.MaProvider;

/**
 * Created by wanglei on 2017/1/4.
 */
@Provider(processName = "com.spinytech.maindemo:pic")
public class PicProvider extends MaProvider{
    @Override
    protected String getName() {
        return "pic";
    }
}
