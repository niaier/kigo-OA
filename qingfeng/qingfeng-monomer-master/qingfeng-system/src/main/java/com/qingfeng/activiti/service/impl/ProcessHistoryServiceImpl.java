package com.qingfeng.activiti.service.impl;

import com.qingfeng.activiti.service.ProcessHistoryService;
import com.qingfeng.utils.Page;
import org.activiti.engine.HistoryService;
import org.activiti.engine.history.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anxingtao on 2020-8-18.
 */
@Service
public class ProcessHistoryServiceImpl implements ProcessHistoryService {

    @Autowired
    private HistoryService historyService;

    /** 
     * @Description: getHistoryTaskList 获取流程的历史任务
     * @Param: [finished, processInstanceId] 
     * @return: java.util.List<org.activiti.engine.history.HistoricTaskInstance> 
     * @Author: anxingtao
     * @Date: 2020-8-18 23:17 
     */ 
    public List<HistoricTaskInstance> getHistoryTaskList(String finished, String processInstanceId) {
        List<HistoricTaskInstance> list = new ArrayList<>();
        if (finished.equals("0")){
            list = historyService // 历史任务Service
                    .createHistoricTaskInstanceQuery() // 创建历史任务实例查询
                    .orderByHistoricTaskInstanceStartTime().asc() //排序
                    //.taskAssignee(GlobalConfig.getOperator()) // 指定办理人
                    .processInstanceId(processInstanceId)
                    .finished() // 查询已经完成的任务
                    .list();
        }else if (finished.equals("1")){
            list = historyService // 历史任务Service
                    .createHistoricTaskInstanceQuery() // 创建历史任务实例查询
                    .orderByHistoricTaskInstanceStartTime().asc()
                    //.taskAssignee(GlobalConfig.getOperator()) // 指定办理人
                    .processInstanceId(processInstanceId)
                    .unfinished()// 查询未完成的任务
                    .list();
        }else{
            list = historyService // 历史任务Service
                    .createHistoricTaskInstanceQuery() // 创建历史任务实例查询
                    .orderByHistoricTaskInstanceStartTime().asc()
                    //.taskAssignee(GlobalConfig.getOperator()) // 指定办理人
                    .processInstanceId(processInstanceId)
                    .list();
        }
        return list;
    }

    /**
     * @Description: getHistoryActInstanceList 获取流程的历史活动
     * @Param: [finished, processInstanceId]
     * @return: java.util.List<org.activiti.engine.history.HistoricActivityInstance>
     * @Author: anxingtao
     * @Date: 2020-8-18 23:16
     */
    public List<HistoricActivityInstance> getHistoryActInstanceList(String finished, String processInstanceId) {
        List<HistoricActivityInstance> list = new ArrayList<>();
        if (finished.equals("0")){
            list = historyService // 历史任务Service
                    .createHistoricActivityInstanceQuery()
                    .orderByHistoricActivityInstanceStartTime().asc()
                    .processInstanceId(processInstanceId)
                    .finished()
                    .list();
        }else if (finished.equals("1")){
            list = historyService // 历史任务Service
                    .createHistoricActivityInstanceQuery()
                    .orderByHistoricActivityInstanceStartTime().asc()
                    .processInstanceId(processInstanceId)
                    .unfinished()
                    .list();
        }else{
            list = historyService // 历史任务Service
                    .createHistoricActivityInstanceQuery()
                    .orderByHistoricActivityInstanceStartTime().asc()
                    .processInstanceId(processInstanceId)
                    .list();
        }
        return list;
    }


    /** 
     * @Description: getHistoryProcessVariables 获取流程历史流程变量
     * @Param: [processInstanceId] 
     * @return: java.util.List<org.activiti.engine.history.HistoricVariableInstance> 
     * @Author: anxingtao
     * @Date: 2020-8-18 23:20 
     */ 
    public List<HistoricVariableInstance> getHistoryProcessVariables(String processInstanceId) {
        List<HistoricVariableInstance> list = null;
        list = historyService
                .createHistoricVariableInstanceQuery()//创建一个历史的流程变量查询对象
                .processInstanceId(processInstanceId)//
                .list();
        return list;
    }


    /**
     * @Description: getFinishedInstanceList 获取已归档的流程实例
     * @Param: [pageNum, pageSize, processDefinitionId]
     * @return: Page
     * @Author: anxingtao
     * @Date: 2020-8-18 23:24
     */
    public Page getFinishedInstanceList(int pageNum, int pageSize, String processDefinitionId) {
        Page page = new Page();
        List<HistoricProcessInstance> list = new ArrayList<>();
        HistoricProcessInstanceQuery historicProcessInstanceQuery = historyService.createHistoricProcessInstanceQuery();

        if (processDefinitionId != null ) {
            historicProcessInstanceQuery =  historicProcessInstanceQuery.processDefinitionId(processDefinitionId);
        }
        int firstResult = (pageNum-1)*pageSize;
        list = historicProcessInstanceQuery
                .orderByProcessInstanceStartTime().asc()//排序
                .finished()
                .listPage(firstResult,pageSize);

        int total = (int) historicProcessInstanceQuery.count();
        page.setTotalResult(total);
        page.setObject(list);
        return page;
    }


    public Page queryHistoricInstance(int pageNum, int pageSize,String processDefinitionId) {
        Page page = new Page();
        List<HistoricProcessInstance> list = new ArrayList<>();
        HistoricProcessInstanceQuery historicProcessInstanceQuery = historyService.createHistoricProcessInstanceQuery();
        if (processDefinitionId != null ) {
            historicProcessInstanceQuery =  historicProcessInstanceQuery.processDefinitionId(processDefinitionId);
        }
        int firstResult = (pageNum-1)*pageSize;
        list = historicProcessInstanceQuery
                .orderByProcessInstanceStartTime().asc()//排序
                .listPage(firstResult,pageSize);
        int total = (int) historicProcessInstanceQuery.count();
        page.setTotalResult(total);
        page.setObject(list);
        return page;
    }



}
