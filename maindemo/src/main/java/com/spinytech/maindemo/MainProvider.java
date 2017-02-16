package com.spinytech.maindemo;

import com.linked.annotion.Provider;
import com.spinytech.macore.MaProvider;

/**
 * Created by wanglei on 2016/12/28.
 */
@Provider(processName = "com.spinytech.maindemo")
public class MainProvider extends MaProvider {
    @Override
    protected String getName() {
        return "main";
    }
}
