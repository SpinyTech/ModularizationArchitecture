package com.spinytech.macore.router;
import com.spinytech.macore.router.MaActionResult;
import com.spinytech.macore.router.RouterRequest;

interface ILocalRouterAIDL {
    boolean checkResponseAsync(in RouterRequest routerRequset);
    MaActionResult route(in RouterRequest routerRequest);
    boolean stopWideRouter();
    void connectWideRouter();
}
