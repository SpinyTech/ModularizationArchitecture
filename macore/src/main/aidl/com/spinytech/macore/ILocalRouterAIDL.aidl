package com.spinytech.macore;


interface ILocalRouterAIDL {
    boolean checkResponseAsync(String routerRequset);
    String route(String routerRequest);
}
