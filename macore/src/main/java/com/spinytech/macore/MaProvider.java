package com.spinytech.macore;

import java.util.HashMap;

/**
 * Created by wanglei on 2016/11/29.
 */

public abstract class MaProvider{
    //TODO this field is used for control the provider on and off
    private boolean mValid = true;
    private HashMap<String,MaAction> mActions;
    public MaProvider(){
        mActions = new HashMap<>();
        registerActions();
    }
    protected void registerAction(String actionName,MaAction action){
        mActions.put(actionName,action);
    }

    public MaAction findAction(String actionName){
        return mActions.get(actionName);
    }

    public boolean isValid(){
        return mValid;
    }

    protected abstract void registerActions();
}
