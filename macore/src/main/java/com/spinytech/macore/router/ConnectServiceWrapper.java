package com.spinytech.macore.router;

/**
 * Created by wanglei on 2016/11/30.
 */

public class ConnectServiceWrapper {
    public boolean needAutoConnect = false;
    public Class<? extends LocalRouterConnectService> targetClass = null;

    public ConnectServiceWrapper(boolean needAutoConnect, Class<? extends LocalRouterConnectService> logicClass) {
        this.needAutoConnect = needAutoConnect;
        this.targetClass = logicClass;
    }
}
