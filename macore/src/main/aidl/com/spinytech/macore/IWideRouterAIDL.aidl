// IRouterAIDL.aidl
package com.spinytech.macore;

// Declare any non-default types here with import statements

interface IWideRouterAIDL {
    boolean checkResponseAsync(String routerRequset);
    String route(String routerRequest);
    boolean shutdownRouter(String domain);
}
