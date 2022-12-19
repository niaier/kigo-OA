package com.qingfeng.activiti.listener;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;

import java.io.Serializable;

/**
 * @author Administrator
 * @version 1.0.0
 * @ProjectName qingfeng-cloud
 * @Description TODO
 * @createTime 2021年10月07日 10:55:00
 */
public class MyExecutionListener implements ExecutionListener, Serializable {
    @Override
    public void notify(DelegateExecution execution) {
        System.out.println("============executionListener start============");
        String eventName = execution.getEventName();
        String currentActivitiId = execution.getCurrentActivityId();
        System.out.println("事件名称:" + eventName);
        System.out.println("ActivitiId:" + currentActivitiId);
        System.out.println("============executionListener  end============");
    }
}
