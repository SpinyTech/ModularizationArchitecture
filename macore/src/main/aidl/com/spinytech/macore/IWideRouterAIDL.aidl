// IRouterAIDL.aidl
package com.spinytech.macore;

// Declare any non-default types here with import statements

interface IWideRouterAIDL {
    boolean checkResponseAsync(String domain,String routerRequset);
    String route(String domain,String routerRequest);
    boolean stopRouter(String domain);
}
