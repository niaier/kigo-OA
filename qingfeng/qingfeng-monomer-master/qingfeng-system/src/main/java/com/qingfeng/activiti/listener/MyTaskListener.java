package com.qingfeng.activiti.listener;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

/**
 * @author Administrator
 * @version 1.0.0
 * @ProjectName qingfeng-cloud
 * @Description TODO
 * @createTime 2021年10月07日 10:56:00
 */
public class MyTaskListener implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        System.out.println("============TaskListener start============");
        String taskDefinitionKey = delegateTask.getTaskDefinitionKey();
        String eventName = delegateTask.getEventName();
        System.out.println("事件名称:" + eventName);
        System.out.println("taskDefinitionKey:" + taskDefinitionKey);
        System.out.println("============TaskListener end============");
    }
}
