package com.spinytech.macore;
import com.spinytech.macore.MaActionResult;

interface ILocalRouterAIDL {
    boolean checkResponseAsync(String routerRequset);
    MaActionResult route(String routerRequest);
    boolean stopWideRouter();
    void connectWideRouter();
}
