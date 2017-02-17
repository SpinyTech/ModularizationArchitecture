// IRouterAIDL.aidl
package com.spinytech.macore.router;
import com.spinytech.macore.router.MaActionResult;
import com.spinytech.macore.router.RouterRequest;
// Declare any non-default types here with import statements

interface IWideRouterAIDL {
    boolean checkResponseAsync(String domain,in RouterRequest routerRequset);
    MaActionResult route(String domain,in RouterRequest routerRequest);
    boolean stopRouter(String domain);
}
