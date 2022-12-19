package com.qingfeng.activiti.service.impl;

import com.qingfeng.activiti.entity.vo.AcExecutionEntityImpl;
import com.qingfeng.activiti.service.ProcessService;
import com.qingfeng.activiti.service.impl.image.CustomProcessDiagramGenerator;
import com.qingfeng.utils.Page;
import com.qingfeng.utils.PageData;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.*;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.cmd.NeedsActiveTaskCmd;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.*;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ExecutionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by anxingtao on 2020-8-9.
 */
@Service
public class ProcessServiceImpl implements ProcessService {

    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private ProcessEngine processEngine;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private ManagementService managementService;

    /**
     * @title getFlowImgByProcessDefinitionId
     * @description 根据流程定义获取未启动的流程图
     * @author Administrator
     * @updateTime 2021/9/12 0012 19:06
     */
    public InputStream getFlowImgByProcessDefinitionId(String procDefinitionId) throws Exception {
        if (org.springframework.util.StringUtils.isEmpty(procDefinitionId)) {
            throw new Exception("[异常]-procDefinitionId！");
        }
        InputStream imageStream = null;
        try {
            // 定义流程画布生成器
            CustomProcessDiagramGenerator processDiagramGenerator = new CustomProcessDiagramGenerator();
            // 获取流程定义Model对象
            BpmnModel bpmnModel = repositoryService.getBpmnModel(procDefinitionId);
            // 使用默认配置获得流程图表生成器，并生成追踪图片字符流
            imageStream = processDiagramGenerator.generateDiagramCustom(
                    bpmnModel,
                    new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>(),
                    "宋体", "微软雅黑", "黑体");
            return imageStream;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("通过流程实例ID" + procDefinitionId + "获取流程图时出现异常！", e);
        } finally {
            if (imageStream != null) {
                imageStream.close();
            }
        }
    }

    /**
     * @title getFlowImgByProcInstId
     * @description 根据流程实例获取运行的流程图
     * @author Administrator
     * @updateTime 2021/9/12 0012 19:07
     */
    public InputStream getFlowImgByProcInstId(String procInstId) throws Exception {
        if (org.springframework.util.StringUtils.isEmpty(procInstId)) {
            throw new Exception("[异常]-传入的参数procInstId为空！");
        }
        InputStream imageStream = null;
        try {
            // 通过流程实例ID获取历史流程实例
            //  获取历史流程实例
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(procInstId).singleResult();

            // 通过流程实例ID获取流程中已经执行的节点，按照执行先后顺序排序
            List<HistoricActivityInstance> historicActivityInstanceList = historyService.createHistoricActivityInstanceQuery()
                    .processInstanceId(procInstId).orderByHistoricActivityInstanceId().asc().list();

            // 将已经执行的节点放入高亮显示节点集合
            List<String> highLightedActivityIdList = historicActivityInstanceList.stream()
                    .map(HistoricActivityInstance::getActivityId)
                    .collect(Collectors.toList());

            // 通过流程实例ID获取流程中正在执行的节点
            List<Execution> runningActivityInstanceList = runtimeService.createExecutionQuery()
                    .processInstanceId(procInstId)
                    .list();
            List<String> runningActivityIdList = new ArrayList<>();
            for (Execution execution : runningActivityInstanceList) {
                if (!org.springframework.util.StringUtils.isEmpty(execution.getActivityId())) {
                    runningActivityIdList.add(execution.getActivityId());
                }
            }

            // 定义流程画布生成器
            CustomProcessDiagramGenerator processDiagramGenerator = new CustomProcessDiagramGenerator();

            // 获取流程定义Model对象
            BpmnModel bpmnModel = repositoryService.getBpmnModel(historicProcessInstance.getProcessDefinitionId());

            // 获取已经流经的流程线，需要高亮显示流程已经发生流转的线id集合
//            List<String> highLightedFlowsIds = getHighLightedFlows(bpmnModel, historicActivityInstanceList);
            List<String> highLightedFlowsIds = getHighLightedFlowsByIncomingFlows(bpmnModel, historicActivityInstanceList);

            // 根据runningActivityIdList获取runningActivityFlowsIds
            List<String> runningActivityFlowsIds = getRunningActivityFlowsIds(bpmnModel, runningActivityIdList, historicActivityInstanceList);

            // 使用默认配置获得流程图表生成器，并生成追踪图片字符流
            imageStream = processDiagramGenerator.generateDiagramCustom(
                    bpmnModel,
                    highLightedActivityIdList, runningActivityIdList, highLightedFlowsIds, runningActivityFlowsIds,
                    "宋体", "微软雅黑", "黑体");
            return imageStream;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("通过流程实例ID" + procInstId + "获取流程图时出现异常！", e);
        } finally {
            if (imageStream != null) {
                imageStream.close();
            }
        }
    }

    /**
     * @param bpmnModel                    bpmnModel
     * @param historicActivityInstanceList historicActivityInstanceList
     * @return HighLightedFlows
     */
    public List<String> getHighLightedFlows(BpmnModel bpmnModel,
                                            List<HistoricActivityInstance> historicActivityInstanceList) {
        //historicActivityInstanceList 是 流程中已经执行的历史活动实例
        // 已经流经的顺序流，需要高亮显示
        List<String> highFlows = new ArrayList<>();

        // 全部活动节点
        List<FlowNode> allHistoricActivityNodeList = new ArrayList<>();

        // 拥有endTime的历史活动实例，即已经完成了的节点
        List<HistoricActivityInstance> finishedActivityInstancesList = new ArrayList<>();

        /*
         * 循环的目的：
         *           获取所有的历史节点FlowNode并放入allHistoricActivityNodeList
         *           获取所有确定结束了的历史节点finishedActivityInstancesList
         */
        for (HistoricActivityInstance historicActivityInstance : historicActivityInstanceList) {
            // 获取流程节点
            // bpmnModel.getMainProcess()获取一个Process对象
            FlowNode flowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(historicActivityInstance.getActivityId(), true);
            allHistoricActivityNodeList.add(flowNode);

            // 如果结束时间不为空，表示当前节点已经完成
            if (historicActivityInstance.getEndTime() != null) {
                finishedActivityInstancesList.add(historicActivityInstance);
            }
        }

        FlowNode currentFlowNode;
        FlowNode targetFlowNode;
        HistoricActivityInstance currentActivityInstance;

        // 遍历已经完成的活动实例，从每个实例的outgoingFlows中找到已经执行的
        for (int k = 0; k < finishedActivityInstancesList.size(); k++) {
            currentActivityInstance = finishedActivityInstancesList.get(k);

            // 获得当前活动对应的节点信息以及outgoingFlows信息
            currentFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(currentActivityInstance.getActivityId(), true);

            // 当前节点的所有流出线
            List<SequenceFlow> outgoingFlowList = currentFlowNode.getOutgoingFlows();

            /*
             * 遍历outgoingFlows并找到已经流转的  满足如下条件认为已经流转：
             * 1、当前节点是并行网关或者兼容网关，则通过outgoingFlows能够在历史活动中找到的全部节点均为已经流转
             * 2、当前节点是以上两种类型之外的，通过outgoingFlows查找到的时间最早的流转节点视为有效流转
             * (第二点有问题，有过驳回的，会只绘制驳回的流程线，通过走向下一级的流程线没有高亮显示)
             */
            if ("parallelGateway".equals(currentActivityInstance.getActivityType()) ||
                    "inclusiveGateway".equals(currentActivityInstance.getActivityType())) {
                // 遍历历史活动节点，找到匹配流程目标节点的
                for (SequenceFlow outgoingFlow : outgoingFlowList) {
                    // 获取当前节点流程线对应的下一级节点
                    targetFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(outgoingFlow.getTargetRef(), true);

                    // 如果下级节点包含在所有历史节点中，则将当前节点的流出线高亮显示
                    if (allHistoricActivityNodeList.contains(targetFlowNode)) {
                        highFlows.add(outgoingFlow.getId());
                    }
                }
            } else {
                /*
                 * 2、当前节点不是并行网关或者兼容网关
                 * 【已解决-问题】如果当前节点有驳回功能，驳回到申请节点，
                 * 则因为申请节点在历史节点中，导致当前节点驳回到申请节点的流程线被高亮显示，但实际并没有进行驳回操作
                 */
                List<Map<String, Object>> tempMapList = new ArrayList<>();

                // 当前节点ID
                String currentActivityId = currentActivityInstance.getActivityId();

                int size = historicActivityInstanceList.size();
                boolean ifStartFind = false;
                boolean ifFinded = false;
                HistoricActivityInstance historicActivityInstance;

                // 循环当前节点的所有流出线
                // 循环所有的历史节点
//                log.info("【开始】-匹配当前节点-ActivityId=【{}】需要高亮显示的流出线", currentActivityId);
//                log.info("循环历史节点");

                for (int i = 0; i < size; i++) {
                    // // 如果当前节点流程线对应的下级节点在历史节点中，则该条流程线进行高亮显示（【问题】有驳回流程线时，即使没有进行驳回操作，因为申请节点在历史节点中，也会将驳回流程线高亮显示-_-||）
                    // if (historicActivityInstance.getActivityId().equals(sequenceFlow.getTargetRef())) {
                    // Map<String, Object> map = new HashMap<>();
                    // map.put("highLightedFlowId", sequenceFlow.getId());
                    // map.put("highLightedFlowStartTime", historicActivityInstance.getStartTime().getTime());
                    // tempMapList.add(map);
                    // // highFlows.add(sequenceFlow.getId());
                    // }

                    // 历史节点
                    historicActivityInstance = historicActivityInstanceList.get(i);
//                    log.info("第【{}/{}】个历史节点-ActivityId=【{}】", i + 1, size, historicActivityInstance.getActivityId());

                    // 如果循环历史节点中的id等于当前节点id，从当前历史节点继续先后查找是否有当前流程线等于的节点
                    // 历史节点的序号需要大于等于已经完成历史节点的序号，放置驳回重审一个节点经过两次时只取第一次的流出线高亮显示，第二次的不显示
                    if (i >= k && historicActivityInstance.getActivityId().equals(currentActivityId)) {
//                        log.info("第【{}】个历史节点和当前节点一致-ActivityId=【{}】", i + 1, historicActivityInstance.getActivityId());
                        ifStartFind = true;
                        // 跳过当前节点继续查找下一个节点
                        continue;
                    }
                    if (ifStartFind) {
//                        log.info("[开始]-循环当前节点-ActivityId=【{}】的所有流出线", currentActivityId);

                        ifFinded = false;
                        for (SequenceFlow sequenceFlow : outgoingFlowList) {
                            // 如果当前节点流程线对应的下级节点在其后面的历史节点中，则该条流程线进行高亮显示
                            // 【问题】
//                            log.info("当前流出线的下级节点=[{}]", sequenceFlow.getTargetRef());
                            if (historicActivityInstance.getActivityId().equals(sequenceFlow.getTargetRef())) {
//                                log.info("当前节点[{}]需高亮显示的流出线=[{}]", currentActivityId, sequenceFlow.getId());
                                highFlows.add(sequenceFlow.getId());
                                // 暂时默认找到离当前节点最近的下一级节点即退出循环，否则有多条流出线时将全部被高亮显示
                                ifFinded = true;
                                break;
                            }
                        }
//                        log.info("[完成]-循环当前节点-ActivityId=【{}】的所有流出线", currentActivityId);
                    }
                    if (ifFinded) {
                        // 暂时默认找到离当前节点最近的下一级节点即退出历史节点循环，否则有多条流出线时将全部被高亮显示
                        break;
                    }
                }
//                log.info("【完成】-匹配当前节点-ActivityId=【{}】需要高亮显示的流出线", currentActivityId);
                // if (!CollectionUtils.isEmpty(tempMapList)) {
                // // 遍历匹配的集合，取得开始时间最早的一个
                // long earliestStamp = 0L;
                // String highLightedFlowId = null;
                // for (Map<String, Object> map : tempMapList) {
                // long highLightedFlowStartTime = Long.valueOf(map.get("highLightedFlowStartTime").toString());
                // if (earliestStamp == 0 || earliestStamp <= highLightedFlowStartTime) {
                // highLightedFlowId = map.get("highLightedFlowId").toString();
                // earliestStamp = highLightedFlowStartTime;
                // }
                // }
                // highFlows.add(highLightedFlowId);
                // }
            }
        }
        return highFlows;
    }

    /**
     * @param bpmnModel                    bpmnModel
     * @param historicActivityInstanceList historicActivityInstanceList
     * @return HighLightedFlows
     */
    public List<String> getHighLightedFlowsByIncomingFlows(BpmnModel bpmnModel,
                                                           List<HistoricActivityInstance> historicActivityInstanceList) {
        //historicActivityInstanceList 是 流程中已经执行的历史活动实例
        // 已经流经的顺序流，需要高亮显示
        List<String> highFlows = new ArrayList<>();

        // 全部活动节点(包括正在执行的和未执行的)
        List<FlowNode> allHistoricActivityNodeList = new ArrayList<>();

        /*
         * 循环的目的：
         *           获取所有的历史节点FlowNode并放入allHistoricActivityNodeList
         *           获取所有确定结束了的历史节点finishedActivityInstancesList
         */
        for (HistoricActivityInstance historicActivityInstance : historicActivityInstanceList) {
            // 获取流程节点
            // bpmnModel.getMainProcess()获取一个Process对象
            FlowNode flowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(historicActivityInstance.getActivityId(), true);
            allHistoricActivityNodeList.add(flowNode);
        }
        // 循环活动节点
        for (FlowNode flowNode : allHistoricActivityNodeList) {
            // 获取每个活动节点的输入线
            List<SequenceFlow> incomingFlows = flowNode.getIncomingFlows();

            // 循环输入线，如果输入线的源头处于全部活动节点中，则将其包含在内
            for (SequenceFlow sequenceFlow : incomingFlows) {
                if (allHistoricActivityNodeList.stream().map(BaseElement::getId).collect(Collectors.toList()).contains(sequenceFlow.getSourceFlowElement().getId())){
                    highFlows.add(sequenceFlow.getId());
                }
            }
        }
        return highFlows;
    }


    private List<String> getRunningActivityFlowsIds(BpmnModel bpmnModel, List<String> runningActivityIdList, List<HistoricActivityInstance> historicActivityInstanceList) {
        List<String> runningActivityFlowsIds = new ArrayList<>();
        List<String> runningActivityIds = new ArrayList<>(runningActivityIdList);
        // 逆序寻找，因为historicActivityInstanceList有序
        if (CollectionUtils.isEmpty(runningActivityIds)) {
            return runningActivityFlowsIds;
        }
        for (int i = historicActivityInstanceList.size() - 1; i >= 0; i--) {
            HistoricActivityInstance historicActivityInstance = historicActivityInstanceList.get(i);
            FlowNode flowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(historicActivityInstance.getActivityId(), true);
            // 如果当前节点是未完成的节点
            if (runningActivityIds.contains(flowNode.getId())) {
                continue;
            }
            // 当前节点的所有流出线
            List<SequenceFlow> outgoingFlowList = flowNode.getOutgoingFlows();
            // 遍历所有的流出线
            for (SequenceFlow outgoingFlow : outgoingFlowList) {
                // 获取当前节点流程线对应的下一级节点
                FlowNode targetFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(outgoingFlow.getTargetRef(), true);
                // 如果找到流出线的目标是runningActivityIdList中的，那么添加后将其移除，避免找到重复的都指向runningActivityIdList的流出线
                if (runningActivityIds.contains(targetFlowNode.getId())) {
                    runningActivityFlowsIds.add(outgoingFlow.getId());
                    runningActivityIds.remove(targetFlowNode.getId());
                }
            }

        }
        return runningActivityFlowsIds;
    }

    /**
     * 解决getRunningActivityFlowsIds对于并行网关不适用的问题
     * 提供者：孙之峰（CSDN)
     */
    public List<String> getRunningActivityFlowIdsByIcommingFlows(BpmnModel bpmnModel, List<String> runningActivityIdList, List<HistoricActivityInstance> historicActivityInstanceList){
        List<String> runningActivityFlowsIds = new ArrayList<>();
        List<String> runningActivityIds = new ArrayList<>(runningActivityIdList);
        // 逆序寻找，因为historicActivityInstanceList有序
        if (CollectionUtils.isEmpty(runningActivityIds)) {
            return runningActivityFlowsIds;
        }

        // 全部活动节点(包括正在执行的和未执行的)
        List<FlowNode> allHistoricActivityNodeList = new ArrayList<>();

        for (HistoricActivityInstance historicActivityInstance : historicActivityInstanceList) {
            // 获取流程节点
            // bpmnModel.getMainProcess()获取一个Process对象
            FlowNode flowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(historicActivityInstance.getActivityId(), true);
            allHistoricActivityNodeList.add(flowNode);
        }

        for (int i = historicActivityInstanceList.size() - 1; i >= 0; i--) {
            HistoricActivityInstance historicActivityInstance = historicActivityInstanceList.get(i);
            FlowNode flowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(historicActivityInstance.getActivityId(), true);
            // 如果当前节点是未完成的节点
            if (runningActivityIds.contains(flowNode.getId())) {
                // 获取每个活动节点的输入线
                List<SequenceFlow> incomingFlows = flowNode.getIncomingFlows();

                // 循环输入线，如果输入线的源头处于全部活动节点中，则将其包含在内
                for (SequenceFlow sequenceFlow : incomingFlows) {
                    if (allHistoricActivityNodeList.stream().map(BaseElement::getId).collect(Collectors.toList()).contains(sequenceFlow.getSourceFlowElement().getId())){
                        runningActivityFlowsIds.add(sequenceFlow.getId());
                    }
                }
            }

        }
        return runningActivityFlowsIds;
    }



    /**
     * @Description: messageStartEventInstance
     * @Param: [message, request]
     * @return: org.activiti.engine.runtime.ProcessInstance
     * @Author: anxingtao
     * @Date: 2020-8-23 18:03
     */
    public ProcessInstance messageStartEventInstance(String message, HttpServletRequest request) {
        Map<String, Object> formProperties = new HashMap<String, Object>();
        // 从request中读取参数然后转换
        Map<String, String[]> parameterMap = request.getParameterMap();
        if (parameterMap.size()>0){
            Set<Map.Entry<String, String[]>> entrySet = parameterMap.entrySet();
            for (Map.Entry<String, String[]> entry : entrySet) {
                String key = entry.getKey();
                // fp_的意思是form paremeter
                if (StringUtils.defaultString(key).startsWith("fp_")) {
                    formProperties.put(key.split("_")[1], entry.getValue()[0]);
                }
            }
        }

        ProcessInstance processInstance = null;
        try {
            //获取当前用户
            String userParams = SecurityContextHolder.getContext().getAuthentication().getName();
            String user_id = userParams.split(":")[1];
            Authentication.setAuthenticatedUserId(user_id);
            processInstance = runtimeService.startProcessInstanceByMessage(message,formProperties);
        } finally {
            Authentication.setAuthenticatedUserId(null);
        }
        return processInstance;
    }

    /**
     * @Description: signalStartEventInstance 可以用来发起启动节点是信号启动类型的流程，也可以用来触发信号边界事件，表单元素key需要以fp_开头
     * @Param: [signalId, executionId, request]
     * @return: org.activiti.engine.runtime.ProcessInstance
     * @Author: anxingtao
     * @Date: 2020-9-5 14:47
     */
    public ProcessInstance signalStartEventInstance(String signalName,String executionId, HttpServletRequest request) {
        Map<String, Object> formProperties = new HashMap<String, Object>();
        // 从request中读取参数然后转换
        Map<String, String[]> parameterMap = request.getParameterMap();
        if (parameterMap.size()>0){
            Set<Map.Entry<String, String[]>> entrySet = parameterMap.entrySet();
            for (Map.Entry<String, String[]> entry : entrySet) {
                String key = entry.getKey();
                // fp_的意思是form paremeter
                if (StringUtils.defaultString(key).startsWith("fp_")) {
                    formProperties.put(key.split("_")[1], entry.getValue()[0]);
                }
            }
        }

        ProcessInstance processInstance = null;
        //流程发起前设置发起人，记录在流程历史中
        //获取当前用户
        String userParams = SecurityContextHolder.getContext().getAuthentication().getName();
        String user_id = userParams.split(":")[1];
        Authentication.setAuthenticatedUserId(user_id);
        //额外拓展：根据发起人车险流程实例
//        historyService.createHistoricProcessInstanceQuery()
//                .startedBy("ketty").singleResult().getProcessDefinitionId();
//        historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult().getStartUserId();//获取发起人

        if (!executionId.isEmpty() && formProperties.size() > 0){
            runtimeService.signalEventReceived(signalName,executionId,formProperties);
        }else if(!executionId.isEmpty()){
            runtimeService.signalEventReceived(signalName,executionId);
        }else{
            runtimeService.signalEventReceived(signalName);
        }
        return processInstance;
    }



    /**
     * @Description: signalEventSubscriptionName 获取某一信号事件的所有执行
     * @Param: [pageNum, pageSize, signalName, processInstanceId]
     * @return: Page
     * @Author: anxingtao
     * @Date: 2020-9-5 14:38
     */
    public Page signalEventSubscriptionName(int pageNum, int pageSize, String signalName, String processInstanceId)  {
        Page page = new Page();

        List<AcExecutionEntityImpl> Acexecutions = new ArrayList<AcExecutionEntityImpl>();

        ExecutionQuery executionQuery = runtimeService.createExecutionQuery();
        if (!processInstanceId.isEmpty()){
            executionQuery = executionQuery.processInstanceId(processInstanceId);
        }
        int firstResult = (pageNum-1)*pageSize;
        List<Execution> executions = executionQuery
                .signalEventSubscriptionName(signalName)
                .listPage(firstResult,pageSize);

        for (int i = 0; i <executions.size(); i++) {
            ExecutionEntityImpl execution = (ExecutionEntityImpl) executions.get(i);
            AcExecutionEntityImpl acExecutionEntity = new AcExecutionEntityImpl();

            acExecutionEntity.setId(execution.getId());
            acExecutionEntity.setActivitiId(execution.getActivityId());
            acExecutionEntity.setActivitiName(execution.getActivityName());
            acExecutionEntity.setParentId(execution.getParentId());
            acExecutionEntity.setProcessDefinitionId(execution.getProcessDefinitionId());
            acExecutionEntity.setProcessDefinitionKey(execution.getProcessDefinitionKey());
            acExecutionEntity.setRootProcessInstanceId(execution.getRootProcessInstanceId());
            acExecutionEntity.setProcessInstanceId(execution.getProcessInstanceId());
            Acexecutions.add(acExecutionEntity);
        }

        //获取总页数
        int total = (int) executionQuery.count();
        page.setShowCount(total);
        page.setObject(Acexecutions);
        return page;
    }



    /**
     * @Description: messageEventSubscriptionName 获取某一消息事件的所有执行  获取消息事件的执行列表
     * @Param: [pageNum, pageSize, messageName, processInstanceId]
     * @return: com.wdata.base.util.Page
     * @Author: anxingtao
     * @Date: 2020-9-5 15:32
     */
    public Page messageEventSubscriptionName(int pageNum, int pageSize, String messageName, String processInstanceId) {
        Page page = new Page();

        List<AcExecutionEntityImpl> Acexecutions = new ArrayList<AcExecutionEntityImpl>();

        ExecutionQuery executionQuery = runtimeService.createExecutionQuery();
        if (!processInstanceId.isEmpty()){
            executionQuery = executionQuery.processInstanceId(processInstanceId);
        }

        int firstResult = (pageNum-1)*pageSize;
        List<Execution> executions = executionQuery
                .messageEventSubscriptionName(messageName)
                .listPage(firstResult,pageSize);

        for (int i = 0; i <executions.size(); i++) {
            ExecutionEntityImpl execution = (ExecutionEntityImpl) executions.get(i);
            AcExecutionEntityImpl acExecutionEntity = new AcExecutionEntityImpl();

            acExecutionEntity.setId(execution.getId());
            acExecutionEntity.setActivitiId(execution.getActivityId());
            acExecutionEntity.setActivitiName(execution.getActivityName());
            acExecutionEntity.setParentId(execution.getParentId());
            acExecutionEntity.setProcessDefinitionId(execution.getProcessDefinitionId());
            acExecutionEntity.setProcessDefinitionKey(execution.getProcessDefinitionKey());
            acExecutionEntity.setRootProcessInstanceId(execution.getRootProcessInstanceId());
            acExecutionEntity.setProcessInstanceId(execution.getProcessInstanceId());
            Acexecutions.add(acExecutionEntity);
        }

        //获取总页数
        int total = (int) executionQuery.count();
        page.setShowCount(total);
        page.setObject(Acexecutions);
        return page;
    }



    /**
     * 消息触发
     * @param messageName
     * @param executionId
     * @param request
     */
    public void messageEventReceived(String messageName, String executionId, HttpServletRequest request) {
        Map<String, Object> formProperties = new HashMap<String, Object>();
        // 从request中读取参数然后转换
        Map<String, String[]> parameterMap = request.getParameterMap();
        if (parameterMap.size()>0){
            Set<Map.Entry<String, String[]>> entrySet = parameterMap.entrySet();
            for (Map.Entry<String, String[]> entry : entrySet) {
                String key = entry.getKey();
                // fp_的意思是form paremeter
                if (StringUtils.defaultString(key).startsWith("fp_")) {
                    formProperties.put(key.split("_")[1], entry.getValue()[0]);
                }
            }
        }

        if (!executionId.isEmpty() && formProperties.size() > 0){
            runtimeService.messageEventReceived(messageName,executionId,formProperties);
        }else if(!executionId.isEmpty()){
            runtimeService.messageEventReceived(messageName,executionId);
        }
    }




    /**
     * 驳回指定节点
     * @param taskId
     * @param flowElementId
     */
    @Transactional(noRollbackFor = Exception.class)
    public void rejectAnyNode(String taskId, String flowElementId,String user_id) {
        FlowNode targetNode = null;
//        System.out.println("流程驳回开始>>>>>>>>>>>>>>>>>>>>");
        //获取当前任务
        Task currentTask = taskService.createTaskQuery().taskId(taskId).singleResult();

        //判断当前用户是否为该节点处理人
        if(currentTask.getAssignee() == null || !currentTask.getAssignee().equals(user_id)){
            throw new ActivitiException("当前用户无法驳回,请先签收任务");
        }

        //获取当前节点信息
        String currActivityId = currentTask.getTaskDefinitionKey();
        String processDefinitionId = currentTask.getProcessDefinitionId();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        FlowNode currFlow = (FlowNode) bpmnModel.getMainProcess().getFlowElement(currActivityId);
        if (null == currFlow) {
            List<SubProcess> subProcessList = bpmnModel.getMainProcess().findFlowElementsOfType(SubProcess.class, true);
            for (SubProcess subProcess : subProcessList) {
                FlowElement flowElement = subProcess.getFlowElement(currActivityId);
                if (flowElement != null) {
                    currFlow = (FlowNode) flowElement;
                    break;
                }
            }
        }

        //获取流程定义
        Process process = repositoryService.getBpmnModel(currentTask.getProcessDefinitionId()).getMainProcess();
        System.out.println("流程驳回>>>>>>>,流程名称:{}:{},当前任务:{}:{}"+process.getName()+process.getId()+currentTask.getName()+currentTask.getId());
        if (flowElementId == null || flowElementId.isEmpty()){
            //获取流程定义第一个Node节点
            targetNode = findFirstActivityNode(currentTask.getProcessDefinitionId());
        }else{
            //获取目标节点定义
            targetNode = (FlowNode)process.getFlowElement(flowElementId);
        }
        if (targetNode == null){
            throw new ActivitiException("目标节点为空");
        }
        //如果不是同一个流程(子流程)不能驳回
        if (!(currFlow.getParentContainer().equals(targetNode.getParentContainer()))) {
            throw new ActivitiException("不是同一个流程(子流程)不能驳回");
        }
        //删除当前运行任务
        String executionEntityId = managementService.executeCommand(new DeleteTaskCmd(currentTask.getId()));
        //流程执行到来源节点
        managementService.executeCommand(new SetFLowNodeAndGoCmd(targetNode, executionEntityId));
        //获取历史办理节点办理人
        List<HistoricTaskInstance> historicTaskInstance = historyService.createHistoricTaskInstanceQuery().processInstanceId(currentTask.getProcessInstanceId()).taskDefinitionKey(flowElementId).orderByHistoricTaskInstanceStartTime().asc().list();
        //查询当前任务
        List<Task> list = taskService.createTaskQuery().processInstanceId(currentTask.getProcessInstanceId()).orderByTaskCreateTime().desc().list();
        for (Task task:list) {
            //根据流程定义id获取bpmnModel对象
            BpmnModel bModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
            //获取当前节点信息
            FlowNode flowNode = (FlowNode) bModel.getFlowElement(task.getTaskDefinitionKey());
            if(flowNode.getId().equals(flowElementId)){
                taskService.setAssignee(task.getId(),historicTaskInstance.get(0).getAssignee());
            }
        }
    }


    //删除当前运行时任务命令，并返回当前任务的执行对象id，这里继承了NeedsActiveTaskCmd，主要时很多跳转业务场景下，要求不能是挂起任务。
    public static class DeleteTaskCmd extends NeedsActiveTaskCmd<String> {
        public DeleteTaskCmd(String taskId){
            super(taskId);
        }
        public String execute(CommandContext commandContext, TaskEntity currentTask){
            //获取所需服务
            TaskEntityManagerImpl taskEntityManager = (TaskEntityManagerImpl)commandContext.getTaskEntityManager();
            //获取当前任务的来源任务及来源节点信息
            ExecutionEntity executionEntity = currentTask.getExecution();
            //删除当前任务,来源任务
            taskEntityManager.deleteTask(currentTask, "流程驳回", false, false);
            return executionEntity.getId();
        }
        public String getSuspendedTaskException() {
            return "挂起的任务不能跳转";
        }
    }

    //根据提供节点和执行对象id，进行跳转命令
    public static class SetFLowNodeAndGoCmd implements Command<Void> {
        private FlowNode flowElement;
        private String executionId;
        public SetFLowNodeAndGoCmd(FlowNode flowElement,String executionId){
            this.flowElement = flowElement;
            this.executionId = executionId;
        }

        public Void execute(CommandContext commandContext){
            //获取目标节点的来源连线
            List<SequenceFlow> flows = flowElement.getIncomingFlows();
            if(flows==null || flows.size()<1){
                throw new ActivitiException("回退错误，目标节点没有来源连线");
            }
            //随便选一条连线来执行，当前执行计划为，从连线流转到目标节点，实现跳转
            ExecutionEntity executionEntity = commandContext.getExecutionEntityManager().findById(executionId);
            executionEntity.setCurrentFlowElement(flows.get(0));
            commandContext.getAgenda().planTakeOutgoingSequenceFlowsOperation(executionEntity, true);
            return null;
        }
    }

    /**
     * 获得流程定义第一个节点
     */
    public FlowNode findFirstActivityNode(String processDefinitionId) {
        BpmnModel model = repositoryService.getBpmnModel(processDefinitionId);
        //Process process = ProcessDefinitionUtil.getProcess(processDefinitionId);
        Process process = model.getMainProcess();
        FlowElement flowElement = process.getInitialFlowElement();
        FlowNode startActivity = (FlowNode) flowElement;

        if (startActivity.getOutgoingFlows().size() != 1) {
            throw new IllegalStateException(
                    "start activity outgoing transitions cannot more than 1, now is : "
                            + startActivity.getOutgoingFlows().size());
        }

        SequenceFlow sequenceFlow = startActivity.getOutgoingFlows()
                .get(0);
        FlowNode targetActivity = (FlowNode) sequenceFlow.getTargetFlowElement();

        if (!(targetActivity instanceof UserTask)) {
            System.out.println("first activity is not userTask, just skip");
            return null;
        }

        return targetActivity;
    }


    /**
     * 任务委托
     * @param taskId
     * @param userId
     */
    public void delegateTask(String taskId, String userId,String userName) {
        taskService.delegateTask(taskId, userId);
        //添加备注
        Task currentTask = taskService.createTaskQuery().taskId(taskId).singleResult();
        taskService.addComment(taskId,currentTask.getProcessInstanceId(),"任务委托给："+userName);
    }


    /**
     * 挂起、激活流程实例
     * @param processInstanceId
     * @param suspendState
     */
    public void suspendProcessInstance(String processInstanceId, String suspendState) {
        if ("2".equals(suspendState)) {
            runtimeService.suspendProcessInstanceById(processInstanceId);
        } else if ("1".equals(suspendState)) {
            runtimeService.activateProcessInstanceById(processInstanceId);
        }
    }


}
