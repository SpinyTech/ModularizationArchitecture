package com.spinytech.maindemo;

import android.util.Pair;

import com.spinytech.macore.MaApplication;
import com.spinytech.macore.multiprocess.BaseApplicationLogic;
import com.spinytech.macore.router.LocalRouterConnectService;
import com.spinytech.macore.router.WideRouter;

import java.util.LinkedList;

/**
 * Created by wanglei on 2016/11/29.
 */

public class MyApplication extends MaApplication {
    public static LinkedList<Pair<String,String>> applicationLogic;
    public static LinkedList<Pair<String,String>> allProcessRouter;

    static {
        applicationLogic = new LinkedList<>();
        applicationLogic.add(new Pair<String, String>("com.spinytech.maindemo", "com.spinytech.maindemo.MainApplicationLogic"));
        applicationLogic.add(new Pair<String, String>("com.spinytech.maindemo", "com.spinytech.webdemo.WebApplicationLogic"));
        applicationLogic.add(new Pair<String, String>("com.spinytech.maindemo:music", "com.spinytech.musicdemo.MusicApplicationLogic"));
        applicationLogic.add(new Pair<String, String>("com.spinytech.maindemo:pic", "com.spinytech.picdemo.PicApplicationLogic"));


        allProcessRouter = new LinkedList<>();
        allProcessRouter.add(new Pair<String, String>("com.spinytech.maindemo", "com.spinytech.maindemo.MainRouterConnectService"));
        allProcessRouter.add(new Pair<String, String>("com.spinytech.maindemo:music", "com.spinytech.musicdemo.MusicRouterConnectService"));
        allProcessRouter.add(new Pair<String, String>("com.spinytech.maindemo:pic", "com.spinytech.picdemo.PicRouterConnectService"));

    }
    @Override
    public void registerAllProcessRouter() {
        for (Pair<String, String> processRouter : allProcessRouter) {
            try {
                Class<? extends LocalRouterConnectService> localRouterClass = (Class<? extends LocalRouterConnectService>) Class.forName(processRouter.second);
                WideRouter.registerLocalRouter(processRouter.first,localRouterClass);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    protected void registerAllApplicationLogic() {
        for (Pair<String, String> processLogic : applicationLogic) {
            try {
                Class<? extends BaseApplicationLogic> applicationLogicClass = (Class<? extends BaseApplicationLogic>) Class.forName(processLogic.second);
                registerApplicationLogic(processLogic.first, 998, applicationLogicClass);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean needMultipleProcess() {
        return true;
    }
}
