package com.spinytech.macore.multiprocess;


/**
 * Created by wanglei on 2016/11/25.
 */

public class PriorityLogicWrapper implements Comparable<PriorityLogicWrapper> {

    public int priority = 0;
    public Class<? extends BaseApplicationLogic> logicClass = null;
    public BaseApplicationLogic instance;

    public PriorityLogicWrapper(int priority, Class<? extends BaseApplicationLogic> logicClass) {
        this.priority = priority;
        this.logicClass = logicClass;
    }

    @Override
    public int compareTo(PriorityLogicWrapper o) {
        return o.priority - this.priority;
    }
}
