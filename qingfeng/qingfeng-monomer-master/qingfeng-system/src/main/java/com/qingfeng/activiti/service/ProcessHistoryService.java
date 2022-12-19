package com.qingfeng.activiti.service;

import com.qingfeng.utils.Page;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;

import java.util.List;

/**
 * Created by anxingtao on 2020-8-18.
 */
public interface ProcessHistoryService {

    /**
     * @Description: getHistoryTaskList 获取流程的历史任务
     * @Param: [finished, processInstanceId]
     * @return: java.util.List<org.activiti.engine.history.HistoricTaskInstance>
     * @Author: anxingtao
     * @Date: 2020-8-18 23:16
     */
    List<HistoricTaskInstance> getHistoryTaskList(String finished, String processInstanceId);

    /**
     * @Description: getHistoryActInstanceList 获取流程的历史活动
     * @Param: [finished, processInstanceId]
     * @return: java.util.List<org.activiti.engine.history.HistoricTaskInstance>
     * @Author: anxingtao
     * @Date: 2020-8-18 23:16
     */
    List<HistoricActivityInstance> getHistoryActInstanceList(String finished, String processInstanceId);

    /**
     * @Description: getHistoryProcessVariables 获取流程历史流程变量
     * @Param: [finished, processInstanceId]
     * @return: java.util.List<org.activiti.engine.history.HistoricVariableInstance>
     * @Author: anxingtao
     * @Date: 2020-8-18 23:20
     */
    List<HistoricVariableInstance> getHistoryProcessVariables(String processInstanceId);

    /**
     * @Description: getFinishedInstanceList 获取已归档的流程实例
     * @Param: [pageNum, pageSize, processDefinitionId]
     * @return: com.wdata.base.util.Page
     * @Author: anxingtao
     * @Date: 2020-8-18 23:23
     */
    Page getFinishedInstanceList(int pageNum, int pageSize, String processDefinitionId);


    /** 
     * @Description: queryHistoricInstance 获取历史流程实例（所有已发起的流程）
     * @Param: [pageNum, pageSize, processDefinitionId] 
     * @return: com.wdata.base.util.Page 
     * @Author: anxingtao
     * @Date: 2020-8-18 23:44
     */ 
    Page queryHistoricInstance(int pageNum, int pageSize, String processDefinitionId);
    

}
