package com.qingfeng.activiti.config;


import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.boot.ProcessEngineConfigurationConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Administrator
 * @version 1.0.0
 * @ProjectName qingfeng-cloud
 * @Description TODO
 * @createTime 2021年10月07日 09:57:00
 */
@Component
public class ActivitiConfig implements ProcessEngineConfigurationConfigurer {

    @Autowired
    private ComActivitiEventListener comActivitiEventListener;

    @Override
    public void configure(SpringProcessEngineConfiguration processEngineConfiguration) {

        List<ActivitiEventListener> activitiEventListener=new ArrayList<ActivitiEventListener>();

        activitiEventListener.add(comActivitiEventListener);//配置全局监听器

        processEngineConfiguration.setEventListeners(activitiEventListener);

    }

}
