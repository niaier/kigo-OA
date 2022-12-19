package com.qingfeng.activiti.service;

import com.qingfeng.utils.Page;
import org.activiti.engine.runtime.ProcessInstance;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.InputStream;

/**
 * Created by anxingtao on 2020-8-9.
 */
public interface ProcessService {

    /**
     * @Description: getFlowImgByProcessDefinitionId 获取流程图
     * @Param: [pProcessInstanceId, response]
     * @return: void
     * @Author: anxingtao
     * @Date: 2020-8-9 22:39
     */
    InputStream getFlowImgByProcessDefinitionId(String procDefinitionId) throws Exception;

    /**
     * @Description: getFlowImgByProcInstId 获取流程图
     * @Param: [pProcessInstanceId, response]
     * @return: void
     * @Author: anxingtao
     * @Date: 2020-8-9 22:39
     */
    InputStream getFlowImgByProcInstId(String procInstId) throws Exception;


    /**
     * @Description: messageStartEventInstance
     * @Param: [messageId, request] 
     * @return: org.activiti.engine.runtime.ProcessInstance 
     * @Author: anxingtao
     * @Date: 2020-8-23 18:02 
     */ 
    public ProcessInstance messageStartEventInstance(String messageId, HttpServletRequest request);


    /**
     * @Description: signalStartEventInstance
     * @Param: [messageId, request]
     * @return: org.activiti.engine.runtime.ProcessInstance
     * @Author: anxingtao
     * @Date: 2020-8-23 18:13
     */
    public ProcessInstance signalStartEventInstance(String signalName, String executionId, HttpServletRequest request);


    /** 
     * @Description: signalEventSubscriptionName 获取某一信号事件的所有执行
     * @Param: [pageNum, pageSize, signalName, processInstanceId] 
     * @return: com.wdata.base.util.Page 
     * @Author: anxingtao
     * @Date: 2020-9-5 14:36 
     */ 
    Page signalEventSubscriptionName(int pageNum, int pageSize, String signalName, String processInstanceId);

    /** 
     * @Description: messageEventSubscriptionName 获取某一消息事件的所有执行
     * @Param: [pageNum, pageSize, messageName, processInstanceId] 
     * @return: com.wdata.base.util.Page 
     * @Author: anxingtao
     * @Date: 2020-9-5 15:32 
     */ 
    Page messageEventSubscriptionName(int pageNum, int pageSize, String messageName, String processInstanceId);

    /**
     * @Description: messageEventReceived 消息触发
     * @Param: [messageName, executionId, request]
     * @return: void
     * @Author: anxingtao
     * @Date: 2020-9-5 15:37
     */
    void messageEventReceived(String messageName, String executionId, HttpServletRequest request);

    /**
     * @Description: rejectAnyNode 驳回流程
     * @Param: [taskId, flowElementId]
     * @return: void
     * @Author: anxingtao
     * @Date: 2020-9-20 23:04
     */
    void rejectAnyNode(String taskId, String flowElementId,String user_id);

    /**
     * @Description: delegateTask 任务委托
     * @Param: [taskId, userId]
     * @return: void
     * @Author: anxingtao
     * @Date: 2020-9-20 23:19
     */
    void delegateTask(String taskId, String userId,String userName);

    /**
     * @Description: suspendProcessInstance 挂起、激活流程实例
     * @Param: [processInstanceId, suspendState]
     * @return: void
     * @Author: anxingtao
     * @Date: 2020-9-20 23:32
     */
    void suspendProcessInstance(String processInstanceId, String suspendState);

}
