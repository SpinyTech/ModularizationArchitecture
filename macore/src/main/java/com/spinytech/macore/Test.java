package com.spinytech.macore;

import com.spinytech.macore.router.LocalRouter;

import java.util.HashMap;
import java.util.List;

/**
 * Created by erfli on 2/15/17.
 */

public class Test {
    private HashMap<String, List<MaProvider>> providerMap;
    private HashMap<String, List<MaAction>> actionMap;

    public void init(String processName) {
        if (providerMap == null) {
            providerMap = new HashMap<String, List<MaProvider>>();
            //put

        }

    }

    public void registerActions(String precessName, String providerName){
        List<MaAction> maActionList = actionMap.get(precessName);
        List<MaProvider> maProviderList = providerMap.get(precessName);

    }
}
