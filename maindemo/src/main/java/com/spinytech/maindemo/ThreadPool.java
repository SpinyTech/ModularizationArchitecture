package com.spinytech.maindemo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by erfli on 2/16/17.
 */

public class ThreadPool {
    private static ExecutorService executorService;
    public static ExecutorService getThreadPoolSingleton(){
        if(executorService == null){
            synchronized (ThreadPool.class){
                executorService = Executors.newFixedThreadPool(3);
            }
        }
        return executorService;
    }
}
