// IRouterAIDL.aidl
package com.spinytech.macore;
import com.spinytech.macore.MaActionResult;
// Declare any non-default types here with import statements

interface IWideRouterAIDL {
    boolean checkResponseAsync(String domain,String routerRequset);
    MaActionResult route(String domain,String routerRequest);
    boolean stopRouter(String domain);
}
