package com.qingfeng.activiti.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.qingfeng.activiti.service.ProcessDefinitionService;
import com.qingfeng.utils.Page;
import org.activiti.engine.*;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

@Service
public class ProcessDefinitionServiceImpl implements ProcessDefinitionService {
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private ManagementService managementService;
    @Autowired
    private ProcessEngine processEngine;
//    DynamicBpmnService dynamicBpmnService = processEngine.getDynamicBpmnService();




    @Transactional
    public void deployProcessDefinition(String filePath) throws FileNotFoundException {
        if (StringUtils.isNotBlank(filePath)) {
            if (filePath.endsWith(".zip") || filePath.endsWith(".bar")) {
                ZipInputStream inputStream = new ZipInputStream(new FileInputStream(filePath));
                repositoryService.createDeployment()
                        .addZipInputStream(inputStream)
                        .deploy();
            } else if (filePath.endsWith(".bpmn")) {
                repositoryService.createDeployment()
                        .addInputStream(filePath, new FileInputStream(filePath))
                        .deploy();
            }
        }
    }


    /** 
     * @Description: findProcessDefinition 查询流程定义列表
     * @Param: [pageNum, pageSize, processDefinitionKey, processDefinitionName] 
     * @return: com.wdata.base.util.Page 
     * @Author: anxingtao
     * @Date: 2020-8-18 23:33 
     */ 
    public Page findProcessDefinition(int pageNum, int pageSize, String processDefinitionKey, String processDefinitionName)  {
        Page page = new Page();
        List<Map<String ,Object>> allList = new ArrayList<>();
        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();//创建一个流程定义查询
        List<ProcessDefinition> processDefinitionList = null;
        if (processDefinitionKey != null) {
            processDefinitionQuery = processDefinitionQuery.processDefinitionKey(processDefinitionKey);//根据流程定义Key查询
        }
        if (processDefinitionName != null) {
            processDefinitionQuery = processDefinitionQuery.processDefinitionNameLike(processDefinitionName);//根据流程定义name查询
        }

        processDefinitionQuery = processDefinitionQuery.orderByProcessDefinitionName().desc()//按照流程定义的名称降序排列
                .orderByProcessDefinitionVersion().desc();//按照版本的降序排列

/*
        int firstResult = (pageNum-1) * pageSize;
        int maxResults = pageSize;
*/
        int firstResult = (pageNum-1)*pageSize;
        processDefinitionList = processDefinitionQuery.listPage(firstResult,pageSize);
        //获取总页数
        int total = (int) processDefinitionQuery.count();

        for (ProcessDefinition processDefinition : processDefinitionList) {
            Map<String ,Object> map = new HashMap<>();
            String deploymentId = processDefinition.getDeploymentId();
            Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(deploymentId).singleResult();
            map.put("processDefinition", BeanUtil.beanToMap(processDefinition));
            map.put("deployment",BeanUtil.beanToMap(deployment));
            allList.add(map);
        }
        page.setTotalResult(total);
        page.setObject(allList);
        return page;
    }
    

}
