package com.qingfeng.activiti.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.XmlUtil;
import com.alibaba.fastjson.JSONArray;
import com.qingfeng.activiti.controller.util.DeleteTaskCommand;
import com.qingfeng.activiti.controller.util.JumpCommand;
import com.qingfeng.activiti.service.IAssignmentService;
import com.qingfeng.activiti.service.ProcessService;
import com.qingfeng.base.controller.BaseController;
import com.qingfeng.entity.activiti.CheckTask;
import com.qingfeng.entity.gencode.Leave;
import com.qingfeng.exception.MyException;
import com.qingfeng.system.service.IUserService;
import com.qingfeng.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.*;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.bpmn.behavior.ParallelMultiInstanceBehavior;
import org.activiti.engine.impl.bpmn.behavior.SequentialMultiInstanceBehavior;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.mvel2.MVEL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @title ProcessTaskController
 * @description 流程任务
 * @author Administrator
 * @updateTime 2021/8/17 0017 21:39
 */
@Slf4j
@Validated
@Controller
@RequestMapping(value = "/activiti/processTask")
public class ProcessTaskController extends BaseController {

    @Autowired
    private ProcessService processService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private IUserService userService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private ManagementService managementService;
//    @Autowired
//    private UserDetailsService userDetailsService;
    @Autowired
    private IAssignmentService assignmentService;
    @Autowired
    private HistoryService historyService;

    /**
     * @title findTaskList
     * @description taskList 获取当前人员待办列表【获取当前人员待办列表,如果要查询所有，则传all】
     * @author Administrator
     * @updateTime 2021/8/17 0017 21:40
     */
    @GetMapping(value = "/findTaskListPage")
    @PreAuthorize("hasAnyAuthority('processTask:info')")
    public void findTaskList(Page page, HttpServletRequest request, HttpServletResponse response, HttpSession session) throws Exception {
        PageData pd = new PageData(request);
        //获取当前用户
        String userParams = SecurityContextHolder.getContext().getAuthentication().getName();
        String user_id = userParams.split(":")[1];
        String organize_id = userParams.split(":")[2];
        //处理分页
        if(Verify.verifyIsNotNull(pd.get("page"))){
            page.setIndex(Integer.parseInt(pd.get("page").toString()));
        }else{
            page.setIndex(1);
        }
        if(Verify.verifyIsNotNull(pd.get("limit"))){
            page.setShowCount(Integer.parseInt(pd.get("limit").toString()));
        }else{
            page.setShowCount(10);
        }
        int firstResult = (page.getIndex()-1)*page.getShowCount();
//        UserQueryImpl user = new UserQueryImpl();
//        user = (UserQueryImpl)identityService.createUserQuery().userId(GlobalConfig.getOperator());
        //查询代签收：taskCandidateUser
        //查询待完成：taskAssignee
        List<Task> tasks = new ArrayList<Task>();
        TaskQuery taskQuery = taskService.createTaskQuery();
        if(Verify.verifyIsNotNull(pd.get("name"))){
            taskQuery = taskQuery.taskNameLike(pd.get("name").toString());
        }
        if(Verify.verifyIsNotNull(pd.get("key"))){
            taskQuery = taskQuery.taskDefinitionKeyLike(pd.get("key").toString());
        }
        if(pd.get("type").equals("userTask")){
            if(Verify.verifyIsNotNull(pd.get("status"))){
                if(pd.get("status").equals("0")){//查询代签收
                    //获取当前用户
                    if (Verify.verifyIsNotNull(pd.get("processDefinitionKey"))) {
                        taskQuery = taskQuery.processDefinitionKey(pd.get("processDefinitionKey").toString())
                                .taskCandidateUser(user_id);
                    } else {
                        taskQuery = taskQuery.taskCandidateUser(user_id);
                    }
                }else if(pd.get("status").equals("1")){//查询代完成
                    //获取当前用户
                    if (Verify.verifyIsNotNull(pd.get("processDefinitionKey"))) {
                        taskQuery = taskQuery.processDefinitionKey(pd.get("processDefinitionKey").toString())
                                .taskAssignee(user_id);
                    } else {
                        taskQuery = taskQuery.taskAssignee(user_id);
                    }
                }
            }else{//查询全部
                //获取当前用户
                if (Verify.verifyIsNotNull(pd.get("processDefinitionKey"))) {
                    taskQuery = taskQuery.processDefinitionKey(pd.get("processDefinitionKey").toString())
                            .taskCandidateOrAssigned(user_id);
                } else {
                    taskQuery = taskQuery.taskCandidateOrAssigned(user_id);
                }
            }
        }else if(pd.get("type").equals("allTask")){
//            taskQuery = taskQuery;
            if (Verify.verifyIsNotNull(pd.get("processInstanceId"))) {
                taskQuery = taskQuery.processInstanceId(pd.get("processInstanceId").toString());
            }
        }

        tasks = taskQuery.active().orderByTaskCreateTime().desc().orderByTaskId().desc().listPage(firstResult,page.getShowCount());
        //获取总页数
        int total = (int) taskQuery.count();
        List<Map> allList = new ArrayList<Map>();
        for (Task task : tasks) {
            Map p = BeanUtil.beanToMap(task);
            System.out.println(p.toString());
            p.put("id",task.getId());
            //查询代办用户及代办用户组名称
            if(Verify.verifyIsNotNull(p.get("assignee"))){
                //根据用户id查询
                pd.put("user_id",p.get("assignee"));
                PageData userPd = userService.findUserInfo(pd);
                if(Verify.verifyIsNotNull(userPd)){
                    p.put("assignee_name",userPd.get("name"));
                    p.put("status","1");//已签收
                }else{
                    p.put("assignee_name",p.get("assignee"));
                    p.put("status","1");//已签收
                }
            }else{
                p.put("status","0");//未签收
            }
            //获取流程实例
            ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
            p.put("businessKey",pi.getBusinessKey());
            String start_user_id = pi.getStartUserId();
            pd.put("user_id", start_user_id.split("_")[1]);
            PageData uPd = userService.findUserInfo(pd);
            p.put("start_user_name", uPd.get("name"));
            p.put("start_time", pi.getStartTime());

            ProcessDefinition procdef = repositoryService.createProcessDefinitionQuery().processDefinitionId(pi.getProcessDefinitionId()).singleResult();
            p.put("procdef_name",procdef.getName());
            allList.add(p);
        }
        Json json = new Json();
        json.setMsg("获取数据成功。");
        json.setData(allList);
        json.setCount(total);
        json.setSuccess(true);
        this.writeJson(response,json);
    }



    /**
     * 生成流程图
     * @param httpServletResponse response entity
     */
    @GetMapping("/readResource")
    public void readResource(HttpServletRequest request,HttpServletResponse httpServletResponse){
        PageData pd = new PageData(request);
        String processInstanceId = pd.get("processInstanceId").toString();
        if (org.springframework.util.StringUtils.isEmpty(processInstanceId)) {
            System.out.println("processInstanceId为空");
        }
        try {
            InputStream img = processService.getFlowImgByProcInstId(processInstanceId);
            byte[] bytes = IOUtils.toByteArray(img);
            httpServletResponse.setContentType("image/svg+xml");
            OutputStream outputStream = httpServletResponse.getOutputStream();
            outputStream.write(bytes);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /**
     * @title claimTask
     * @description claim签收任务
     * @author Administrator
     * @updateTime 2021/8/17 0017 21:40
     */
    @PostMapping("/claimTask")
    @PreAuthorize("hasAnyAuthority('processTask:claimTask')")
    public void claimTask(@RequestBody PageData pd, HttpServletResponse response) throws Exception {
        //获取当前用户
        String userParams = SecurityContextHolder.getContext().getAuthentication().getName();
        taskService.claim(pd.get("taskId").toString(), userParams.split(":")[1]);
        Json json = new Json();
        json.setMsg("任务签收成功。");
        json.setSuccess(true);
        this.writeJson(response,json);
    }


    /** 
     * @Description: completeTask 办理任务，提交task，并保存form
     * @Param: [request, response] 
     * @return: void 
     * @Author: anxingtao
     * @Date: 2020-8-15 21:36 
     */
    @PostMapping("/completeTask")
    @PreAuthorize("hasAnyAuthority('processTask:completeTask')")
    public void completeTask(@RequestBody PageData pd,HttpServletRequest request, HttpServletResponse response) throws Exception {
        String userParams = SecurityContextHolder.getContext().getAuthentication().getName();

        String comment = pd.get("comment").toString();
        String taskId = pd.get("taskId").toString();
        String processInstanceId = pd.get("processInstanceId").toString();
//        String type = pd.get("type").toString();
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        Map<String, Object> formProperties = new HashMap<String, Object>();
        // 从request中读取参数然后转换
        Map<String, String[]> parameterMap = request.getParameterMap();
        Set<Map.Entry<String, String[]>> entrySet = parameterMap.entrySet();
        for (Map.Entry<String, String[]> entry : entrySet) {
            String key = entry.getKey();
            // fp_的意思是form paremeter
            if (StringUtils.defaultString(key).startsWith("fp_")) {
                formProperties.put(key.split("_")[1], entry.getValue()[0]);
            }
        }
        System.out.println(formProperties);
        try {
            //设置发起人id
            Authentication.setAuthenticatedUserId(userParams.split(":")[2]+"_"+userParams.split(":")[1]);
            //添加评论
            if (!comment.isEmpty()){
                taskService.addComment(taskId,processInstanceId,comment);
            }
            //任务委托处理
            DelegationState delegationState = task.getDelegationState();
            if(Verify.verifyIsNotNull(delegationState)){
                if (delegationState.toString().equals("PENDING")){
                    taskService.resolveTask(taskId,formProperties);
                }
            }
            taskService.complete(taskId, formProperties);
//            formService.submitTaskFormData(pd.get("taskId").toString(), formProperties);
        } finally {
            Authentication.setAuthenticatedUserId(null);
        }

        //动态指定下一任务办理人
        List<Task> taskList = taskService.createTaskQuery().processInstanceId(pd.get("processInstanceId").toString()).list();
        for (Task task1:taskList) {
            task1.setAssignee("2");
            taskService.setAssignee(task1.getId(),userParams.split(":")[1]);//设置代理人
            taskService.addCandidateGroup(task1.getId(),userParams.split(":")[2]);//设置候选组
            taskService.addCandidateUser(task1.getId(),userParams.split(":")[1]);//设置候选人
        }
        Json json = new Json();
        json.setMsg("任务办理成功。");
        json.setSuccess(true);
        this.writeJson(response,json);
    }


    /** 
     * @Description: messageStartEventInstance 发起启动节点是消息启动类型的流程，key需要以fp_开头
     * @Param: [map, request, response] 
     * @return: void 
     * @Author: anxingtao
     * @Date: 2020-8-23 18:01 
     */
    @PostMapping("/messageStartEventInstance")
    @PreAuthorize("hasAnyAuthority('processTask:messageStartEventInstance')")
    public void messageStartEventInstance(@RequestBody PageData pd, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ProcessInstance processInstance = processService.messageStartEventInstance(pd.get("messageId").toString(),request);
        Json json = new Json();
        json.setMsg("流程启动成功。");
        json.setSuccess(true);
        this.writeJson(response,json);
    }

    /**
     * @Description: signalStartEventInstance 发起启动节点是信号启动类型的流程，key需要以fp_开头
     * @Param: [map, request, response]
     * @return: void
     * @Author: anxingtao
     * @Date: 2020-8-23 18:12
     */
    @PostMapping("/signalStartEventInstance")
    @PreAuthorize("hasAnyAuthority('processTask:signalStartEventInstance')")
    public void signalStartEventInstance(@RequestBody PageData pd, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ProcessInstance processInstance = processService.signalStartEventInstance(pd.get("signalName").toString(),pd.get("executionId").toString(),request);
        Json json = new Json();
        json.setMsg("流程启动成功。");
        json.setSuccess(true);
        this.writeJson(response,json);
    }


    /** 
     * @Description: signalEventSubscriptionName 获取某一信号事件的所有执行 
     * @Param: [map, request, response] 
     * @return: void 
     * @Author: anxingtao
     * @Date: 2020-9-5 14:34 
     */
    @PostMapping("/getSignalEventSubscription")
    @PreAuthorize("hasAnyAuthority('processTask:getSignalEventSubscription')")
    public void signalEventSubscriptionName(@RequestBody PageData pd, HttpServletRequest request, HttpServletResponse response) throws Exception {
        int pageNum = Integer.parseInt(pd.get("pageNum").toString());
        int pageSize = Integer.parseInt(pd.get("pageSize").toString());
        String signalName = pd.get("signalName").toString();
        String processInstanceId = pd.get("processInstanceId").toString();
        Page page = processService.signalEventSubscriptionName(pageNum, pageSize, signalName, processInstanceId);
        Json json = new Json();
        json.setMsg("信号事件获取成功。");
        json.setData(page);
        json.setSuccess(true);
        this.writeJson(response,json);
    }


    /**
     * @Description: messageEventSubscriptionName 获取某一消息事件的所有执行
     * @Param: [map, request, response]
     * @return: void
     * @Author: anxingtao
     * @Date: 2020-9-5 15:30
     */
    @PostMapping("/getMessageEventSubscription")
    @PreAuthorize("hasAnyAuthority('processTask:getMessageEventSubscription')")
    public void messageEventSubscriptionName(@RequestBody PageData pd, HttpServletRequest request, HttpServletResponse response) throws Exception {
        int pageNum = Integer.parseInt(pd.get("pageNum").toString());
        int pageSize = Integer.parseInt(pd.get("pageSize").toString());
        String messageName = pd.get("messageName").toString();
        String processInstanceId = pd.get("processInstanceId").toString();
        Page page = processService.messageEventSubscriptionName(pageNum, pageSize, messageName, processInstanceId);
        Json json = new Json();
        json.setMsg("消息事件获取成功。");
        json.setData(page);
        json.setSuccess(true);
        this.writeJson(response,json);
    }


    /**
     * @Description: messageEventReceived 消息触发，表单元素key需要以fp_开头
     * @Param: [map, request, response]
     * @return: void
     * @Author: anxingtao
     * @Date: 2020-9-5 15:36
     */
    @PostMapping("/messageEventReceived")
    @PreAuthorize("hasAnyAuthority('processTask:messageEventReceived')")
    public void messageEventReceived(@RequestBody PageData pd, HttpServletRequest request, HttpServletResponse response) throws Exception {
        //消息定义的名称
        String messageName = pd.get("messageName").toString();
        //执行ID
        String executionId = pd.get("executionId").toString();
        processService.messageEventReceived(messageName,executionId,request);
        Json json = new Json();
        json.setMsg("消息触发成功。");
        json.setSuccess(true);
        this.writeJson(response,json);
    }



    /** 
     * @Description: rejectAnyNode 流程驳回
     * @Param: [map, request, response] 
     * @return: void 
     * @Author: anxingtao
     * @Date: 2020-9-20 23:02 
     */
    @GetMapping("/rejectAnyNode")
    @PreAuthorize("hasAnyAuthority('processTask:rejectAnyNode')")
    public void rejectAnyNode(HttpServletRequest request, HttpServletResponse response,HttpSession session) throws Exception {
        PageData pd = new PageData(request);
        //获取当前用户
        String userParams = SecurityContextHolder.getContext().getAuthentication().getName();
        String user_id = userParams.split(":")[1];
        String organize_id = userParams.split(":")[2];
        processService.rejectAnyNode(pd.get("taskId").toString(),pd.get("flowElementId").toString(),user_id);
        Json json = new Json();
        json.setMsg("流程驳回成功。");
        json.setSuccess(true);
        this.writeJson(response,json);
    }


    /**
     * @Description: delegateTask 委托任务
     * @Param: [map, request, response]
     * @return: void
     * @Author: anxingtao
     * @Date: 2020-9-20 23:18
     */
    @GetMapping("/delegateTask")
    @PreAuthorize("hasAnyAuthority('processTask:delegateTask')")
    public void delegateTask(HttpServletRequest request, HttpServletResponse response) throws Exception {
        PageData pd = new PageData(request);
        processService.delegateTask(pd.get("taskId").toString(),pd.get("userId").toString(),pd.get("userName").toString());
        Json json = new Json();
        json.setMsg("委托任务成功。");
        json.setSuccess(true);
        this.writeJson(response,json);
    }


    /**
     * 当流程实例被挂起时，无法通过下一个节点对应的任务id来继续这个流程实例。
     * 通过挂起某一特定的流程实例，可以终止当前的流程实例，而不影响到该流程定义的其他流程实例。
     * 激活之后可以继续该流程实例，不会对后续任务造成影响。
     * 直观变化：流程挂起时 act_ru_task 的 SUSPENSION_STATE_ 为 2
     * 挂起、激活流程实例
     * @return
     */
    @PostMapping("/suspendProcessInstance")
    @PreAuthorize("hasAnyAuthority('processTask:suspendProcessInstance')")
    public void suspendProcessInstance(@RequestBody PageData pd, HttpServletRequest request, HttpServletResponse response) throws Exception {
        //状态： 1、激活 2、挂起
        processService.suspendProcessInstance(pd.get("processInstanceId").toString(), pd.get("suspendState").toString());
        Json json = new Json();
        json.setMsg("挂起、激活流程实例成功。");
        json.setSuccess(true);
        this.writeJson(response,json);
    }



    /** 
     * @Description: shutdownTask 流程终止
     * @Param: [map, request, response] 
     * @return: void 
     * @Author: anxingtao
     * @Date: 2020-11-4 13:09 
     */
    @GetMapping("/shutdownTask")
    @PreAuthorize("hasAnyAuthority('processTask:shutdownTask')")
    public void shutdownTask(ModelMap map, HttpServletRequest request, HttpServletResponse response) throws Exception {
        PageData pd = new PageData(request);
//        runtimeService.deleteProcessInstance(pd.get("processInstanceId").toString(), "删除当前执行任务");
        //判断当前用户是否为该节点处理人
        Task task = taskService.createTaskQuery().taskId(pd.get("taskId").toString()).singleResult();
        //获取当前节点
        String processDefinitionId = task.getProcessDefinitionId();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);

        if(bpmnModel != null) {
            Collection<FlowElement> flowElements = bpmnModel.getMainProcess().getFlowElements();
            for (FlowElement e : flowElements) {
                if (e instanceof org.activiti.bpmn.model.EndEvent) {//如果是网关  则查询网关后的节点
//                    System.out.println("flowelement id:" + e.getId() + "  name:" + e.getName() + "   class:" + e.getClass().toString());
                    Task currentTask = taskService.createTaskQuery().taskId(pd.get("taskId").toString()).singleResult();
                    //获取流程定义
                    Process process = repositoryService.getBpmnModel(currentTask.getProcessDefinitionId()).getMainProcess();
                    //获取目标节点定义
                    FlowNode targetNode = (FlowNode)process.getFlowElement(e.getId());
                    //删除当前运行任务
                    String executionEntityId = managementService.executeCommand(new DeleteTaskCommand(currentTask.getId()));
                    //流程执行到来源节点
                    managementService.executeCommand(new JumpCommand(targetNode, executionEntityId));
                }
            }
        }
        Json json = new Json();
        json.setMsg("终止");
        json.setSuccess(true);
        this.writeJson(response,json);
    }



    @PostMapping("/findNextAssignment")
    public void findNextAssignment(@RequestBody PageData pd,HttpServletRequest request,HttpServletResponse response) throws Exception {

        //获取当前节点信息
        FlowNode flowNode = null;
        if(pd.get("process_status").equals("0")){//未启动
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(pd.getString("processDefinitionKey")).latestVersion().singleResult();
            //根据流程定义id获取bpmnModel对象
            BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinition.getId());
            pd.put("model_id",pd.getString("processDefinitionKey"));

            //Process对象封装了全部的节点、连线、以及关口等信息。拿到这个对象就能够为所欲为了。
            Process process = bpmnModel.getProcesses().get(0);
            //获取全部的FlowElement（流元素）信息
            Collection<FlowElement> flowElements = process.getFlowElements();
            for (FlowElement flowElement : flowElements) {
                //开始节点
                if(flowElement instanceof StartEvent) {
                    flowNode = (FlowNode) flowElement;
                }
            }
        }else{//已启动
            Task task = taskService.createTaskQuery().taskId(pd.get("taskId").toString()).singleResult();

            pd.put("model_id",task.getProcessDefinitionId().split(":")[0]);
            //根据流程定义id获取bpmnModel对象
            BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
            //获取当前节点信息
            flowNode = (FlowNode) bpmnModel.getFlowElement(task.getTaskDefinitionKey());
        }
        List<PageData> list = new ArrayList<PageData>();
        list = findNodeAssignee(list,flowNode,pd);
        System.out.println("--------------------办理人获取完成--------------------");
        System.out.println(list);
        //判断是否是多实例对象
        boolean flag = false;
        if(Verify.verifyIsNotNull(pd.get("taskId"))){//已启动流程
            Task task = taskService.createTaskQuery().taskId(pd.get("taskId").toString()).singleResult();
            BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
            //获取当前活动对象
            FlowElement flowElement = bpmnModel.getFlowElement(task.getTaskDefinitionKey());
            // 根据活动id获取活动实例
            if (flowElement instanceof UserTask) {
                UserTask userTask = (UserTask) flowElement;
                if (userTask.getBehavior() instanceof ParallelMultiInstanceBehavior) {
                    flag = true;
                }else if(userTask.getBehavior() instanceof SequentialMultiInstanceBehavior){
                    flag = true;
                }
            }
        }
        pd.put("node_flag",flag);
        //查询 当前办理人的
        String userParams = SecurityContextHolder.getContext().getAuthentication().getName();
        String user_id = userParams.split(":")[1];
        String organize_id = userParams.split(":")[2];
        pd.put("user_id", user_id);
        PageData uPd = userService.findUserInfo(pd);
        pd.put("depart_leader", uPd.get("depart_leader"));
        pd.put("direct_leader", uPd.get("direct_leader"));
        pd.put("branch_leader", uPd.get("branch_leader"));
        pd.put("depart_leader_name", uPd.get("depart_leader_name"));
        pd.put("direct_leader_name", uPd.get("direct_leader_name"));
        pd.put("branch_leader_name", uPd.get("branch_leader_name"));
        //查询 流程启动人
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(pd.get("processInstanceId").toString()).singleResult();
        if(Verify.verifyIsNotNull(processInstance)) {//已启动流程
            String start_user_id = processInstance.getStartUserId();
            pd.put("user_id", start_user_id.split("_")[1]);
            uPd = userService.findUserInfo(pd);
            pd.put("start_user_id", start_user_id.split("_")[1]);
            pd.put("start_user_name", uPd.get("name"));
            pd.put("start_organize_id", start_user_id.split("_")[0]);
        }else{
            pd.put("start_user_id", user_id);
            pd.put("start_user_name", uPd.get("name"));
            pd.put("start_organize_id", organize_id);
        }

        Json json = new Json();
        json.setData(list);
        json.setObject(pd);
        json.setSuccess(true);
        json.setMsg("数据获取成功。");
        this.writeJson(response,json);
    }



    public List<PageData> findNodeAssignee(List<PageData> list, FlowNode flowNode,PageData pd){
        //获取当前节点输出连线
        List<SequenceFlow> outgoingFlows = flowNode.getOutgoingFlows();
        //遍历输出连线
        for (SequenceFlow outgoingFlow : outgoingFlows) {
            //获取输出节点元素
            FlowElement targetFlowElement = outgoingFlow.getTargetFlowElement();
            //排除非用户任务接点
            System.out.println(targetFlowElement.getName());
            if(targetFlowElement instanceof UserTask){
                PageData p = new PageData();
                p.put("node_id",outgoingFlow.getTargetFlowElement().getId());
                p.put("node_name",outgoingFlow.getTargetFlowElement().getName());
                System.out.println("---------------------用户任务");
                if(!StringUtils.isEmpty(outgoingFlow.getConditionExpression())) {//outgoingFlow上如果没有conditionExpression 表达式，直接处理用户任务
                    Serializable compiled = MVEL.compileExpression(outgoingFlow.getConditionExpression().replace("${","").replace("}",""));
                    Boolean result = MVEL.executeExpression(compiled, pd, Boolean.class);//判断是否会走当前节点
                    if(!result) {//符合条件
                        continue;
                    }
                }
                //查询人员指定
                pd.put("node_key",outgoingFlow.getTargetFlowElement().getId());
                System.out.println("---------------findActivitiAssignment---------------");
                System.out.println(pd.toString());
                PageData assignPd = assignmentService.findActivitiAssignment(pd);
                System.out.println(assignPd);
                if(Verify.verifyIsNotNull(assignPd)){
                    p.putAll(assignPd);
                }

                //判断是否是多实例任务
                String flag = "";
                UserTask userTask = (UserTask) targetFlowElement;
                if (userTask.getBehavior() instanceof ParallelMultiInstanceBehavior) {
                    ParallelMultiInstanceBehavior behavior = (ParallelMultiInstanceBehavior) userTask.getBehavior();
//					if (behavior != null && behavior.getCollectionExpression() != null) {//验证表达式
                    flag = behavior.getCollectionVariable()+":true";

//					}
                }else if (userTask.getBehavior() instanceof SequentialMultiInstanceBehavior) {
                    SequentialMultiInstanceBehavior behavior = (SequentialMultiInstanceBehavior) userTask.getBehavior();
                    flag = behavior.getCollectionVariable()+":true";
                }
                p.put("multiInstances",flag);
                list.add(p);
            }else if(targetFlowElement instanceof InclusiveGateway){//包含网关
                System.out.println("---------------------包含网关");
                //包含网关
                InclusiveGateway gateway = ((InclusiveGateway) targetFlowElement);
                List<SequenceFlow> tmpList = gateway.getOutgoingFlows();//网关后面的连线
                for (SequenceFlow sf2 : tmpList) {//遍历这些连线 并查询到相关的userTask
                    if(!StringUtils.isEmpty(sf2.getConditionExpression())) {//SequenceFlow上如果没有conditionExpression 表达式，直接处理用户任务
                        Serializable compiled = MVEL.compileExpression(sf2.getConditionExpression().replace("${", "").replace("}", ""));
                        Boolean result = MVEL.executeExpression(compiled, pd, Boolean.class);//判断是否会走当前节点
                        if (!result) {//符合条件
                            continue;
                        }
                    }
                    FlowElement targetRef2 = sf2.getTargetFlowElement();
                    if(targetRef2 instanceof UserTask){
                        PageData p = new PageData();
                        p.put("node_id",targetRef2.getId());
                        p.put("node_name",targetRef2.getName());
                        //查询人员指定
                        pd.put("node_key",targetRef2.getId());
                        PageData assignPd = assignmentService.findActivitiAssignment(pd);
                        p.putAll(assignPd);
                        //判断是否是多实例任务
                        String flag = "";
                        UserTask userTask = (UserTask) targetRef2;
                        if (userTask.getBehavior() instanceof ParallelMultiInstanceBehavior) {
                            ParallelMultiInstanceBehavior behavior = (ParallelMultiInstanceBehavior) userTask.getBehavior();
//								if (behavior != null && behavior.getCollectionExpression() != null) {
                            flag = behavior.getCollectionVariable()+":true";
//								}
                        }else if (userTask.getBehavior() instanceof SequentialMultiInstanceBehavior) {
                            SequentialMultiInstanceBehavior behavior = (SequentialMultiInstanceBehavior) userTask.getBehavior();
                            flag = behavior.getCollectionVariable()+":true";
                        }
                        p.put("multiInstances",flag);
                        list.add(p);
                    }
                }
            }else if(targetFlowElement instanceof ParallelGateway){//并行网关
                System.out.println("---------------------并行网关");
                //包含网关
                ParallelGateway gateway = ((ParallelGateway) targetFlowElement);
                List<SequenceFlow> tmpList = gateway.getOutgoingFlows();//网关后面的连线
                for (SequenceFlow sf2 : tmpList) {//遍历这些连线 并查询到相关的userTask
                    FlowElement targetRef2 = sf2.getTargetFlowElement();
                    if(targetRef2 instanceof UserTask){
                        PageData p = new PageData();
                        p.put("node_id",targetRef2.getId());
                        p.put("node_name",targetRef2.getName());
                        //查询人员指定
                        pd.put("node_key",targetRef2.getId());
                        PageData assignPd = assignmentService.findActivitiAssignment(pd);
                        p.putAll(assignPd);

                        //判断是否是多实例任务
                        String flag = "";
                        UserTask userTask = (UserTask) targetRef2;
                        if (userTask.getBehavior() instanceof ParallelMultiInstanceBehavior) {
                            ParallelMultiInstanceBehavior behavior = (ParallelMultiInstanceBehavior) userTask.getBehavior();
//							if (behavior != null && behavior.getCollectionExpression() != null) {
                            flag = behavior.getCollectionVariable()+":true";
//							}
                        }else if (userTask.getBehavior() instanceof SequentialMultiInstanceBehavior) {
                            SequentialMultiInstanceBehavior behavior = (SequentialMultiInstanceBehavior) userTask.getBehavior();
                            flag = behavior.getCollectionVariable()+":true";
                        }
                        p.put("multiInstances",flag);
                        list.add(p);
                    }
                }
            }else if(targetFlowElement instanceof ExclusiveGateway){//排他网关
                System.out.println("---------------------排他网关");
                int i = 0;//用户判断多条满足的情况
                //网关
                ExclusiveGateway gateway = ((ExclusiveGateway) targetFlowElement);
                List<SequenceFlow> tmpList = gateway.getOutgoingFlows();//网关后面的连线
                for (SequenceFlow sf2 : tmpList) {//遍历这些连线 并查询到相关的userTask
                    if(StringUtils.isEmpty(sf2.getConditionExpression())){//SequenceFlow上如果没有conditionExpression 就不去关注
                        continue;
                    }else{
                        Serializable compiled = MVEL.compileExpression(sf2.getConditionExpression().replace("${","").replace("}",""));
                        System.out.println("--------------------------");
                        System.out.println(compiled);
                        System.out.println(sf2.getConditionExpression());
                        System.out.println(pd.toString());
                        Boolean result = MVEL.executeExpression(compiled, pd, Boolean.class);//判断是否会走当前节点
                        System.out.println(result);
                        if(result){//符合条件
                            FlowElement targetRef2 = sf2.getTargetFlowElement();
                            if(i==0){//只取默认第一条。
                                i=1;
                                if(targetRef2 instanceof UserTask){
                                    PageData p = new PageData();
                                    p.put("node_id",targetRef2.getId());
                                    p.put("node_name",targetRef2.getName());
                                    //查询人员指定
                                    pd.put("node_key",targetRef2.getId());
                                    PageData assignPd = assignmentService.findActivitiAssignment(pd);
                                    p.putAll(assignPd);

                                    //判断是否是多实例任务
                                    String flag = "";
                                    UserTask userTask = (UserTask) targetRef2;
                                    if (userTask.getBehavior() instanceof ParallelMultiInstanceBehavior) {
                                        ParallelMultiInstanceBehavior behavior = (ParallelMultiInstanceBehavior) userTask.getBehavior();
//										if (behavior != null && behavior.getCollectionExpression() != null) {
                                        flag = behavior.getCollectionVariable()+":true";
//										}
                                    }else if (userTask.getBehavior() instanceof SequentialMultiInstanceBehavior) {
                                        SequentialMultiInstanceBehavior behavior = (SequentialMultiInstanceBehavior) userTask.getBehavior();
                                        flag = behavior.getCollectionVariable()+":true";
                                    }
                                    p.put("multiInstances",flag);
                                    list.add(p);
                                }
                            }
                        }
                    }
                }

            }else if(targetFlowElement instanceof SubProcess){
                System.out.println("---------------------SubProcess");
                if(!StringUtils.isEmpty(outgoingFlow.getConditionExpression())) {//outgoingFlow上如果没有conditionExpression 表达式，直接处理用户任务
                    Serializable compiled = MVEL.compileExpression(outgoingFlow.getConditionExpression().replace("${","").replace("}",""));
                    Boolean result = MVEL.executeExpression(compiled, pd, Boolean.class);//判断是否会走当前节点
                    if(!result) {//符合条件
                        continue;
                    }
                }
                SubProcess subProcess = ((SubProcess) targetFlowElement);
                for (String key : subProcess.getFlowElementMap().keySet()) {
                    FlowElement flowElement = subProcess.getFlowElementMap().get(key);
                    if(flowElement instanceof  StartEvent){
                        findNodeAssignee(list,(FlowNode) flowElement,pd);
                    }
                }
            }else if(targetFlowElement instanceof EndEvent){
                if(targetFlowElement.getParentContainer() instanceof  SubProcess){
                    System.out.println("------------------------子流程结束");
                    findNodeAssignee(list,(FlowNode) targetFlowElement.getParentContainer(),pd);
                }
            }
        }
        return list;
    }


    /**
     * @title findActivityList
     * @description 查询流程跟踪单
     * @author Administrator
     * @updateTime 2021/9/20 0020 11:44
     */
    @GetMapping("/findActivityList")
    public void findActivityList(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws Exception {
        PageData pd = new PageData(request);
        HistoricProcessInstance processInstance= historyService.createHistoricProcessInstanceQuery().
                processInstanceId(pd.get("processInstanceId").toString()).singleResult();
        //历史行为
        List<HistoricActivityInstance> activityList = new ArrayList<HistoricActivityInstance>();
        HistoricActivityInstanceQuery activityQuery = historyService.createHistoricActivityInstanceQuery();
        if (Verify.verifyIsNotNull(pd.get("processInstanceId"))) {
            activityQuery = activityQuery.processInstanceId(pd.get("processInstanceId").toString());//根据流程实例查询
        }
        activityQuery = activityQuery.orderByHistoricActivityInstanceStartTime().desc();
        activityList = activityQuery.list();
        List<Map<String ,Object>> allList = new ArrayList<>();
        for (HistoricActivityInstance activity : activityList) {
            Map p = BeanUtil.beanToMap(activity);
            p.put("id",activity.getId());
            //查询审批意见
            if(Verify.verifyIsNotNull(activity.getTaskId())){
                List<Comment> comments = taskService.getTaskComments(activity.getTaskId());
                if(comments.size()>0){
                    p.put("comment_msg",comments.get(0).getFullMessage());
                    p.put("comment_time",comments.get(0).getTime());
                }
            }
            //处理办理人
            if(Verify.verifyIsNotNull(activity.getAssignee())){
                pd.put("user_id",activity.getAssignee());
                PageData uPd = userService.findUserInfo(pd);
                p.put("assignee_name",uPd.get("name"));
            }
            if(activity.getActivityType().equals("startEvent")){//获取流程发起人
                String startUser = processInstance.getStartUserId();
                pd.put("user_id",startUser.split("_")[1]);
                PageData uPd = userService.findUserInfo(pd);
                p.put("assignee_name",uPd.get("name"));
            }
            allList.add(p);
        }
        Json json = new Json();
        json.setMsg("获取数据成功。");
        json.setData(allList);
        json.setSuccess(true);
        this.writeJson(response,json);
    }

    /**
     * @title findActivityTaskList
     * @description 查询当前流程任务
     * @author Administrator
     * @updateTime 2021/9/20 0020 12:15
     */
    @GetMapping("/findActivityTaskList")
    public void findActivityTaskList(HttpServletRequest request, HttpServletResponse response) throws Exception {
        PageData pd = new PageData(request);
        List<Map<String ,Object>> allList = new ArrayList<>();
        //查询当前任务
        List<Task> list = taskService.createTaskQuery().processInstanceId(pd.get("processInstanceId").toString()).orderByTaskCreateTime().desc().list();
        for (Task task:list) {
            Map p = BeanUtil.beanToMap(task);
            //处理办理人
            if(Verify.verifyIsNotNull(task.getAssignee())){
                pd.put("user_id",task.getAssignee());
                PageData uPd = userService.findUserInfo(pd);
                p.put("assignee_name",uPd.get("name"));
            }else{
                p.put("assignee_name","");
            }
            if(Verify.verifyIsNotNull(task.getOwner())){
                pd.put("user_id",task.getOwner());
                PageData uPd = userService.findUserInfo(pd);
                p.put("owner_name",uPd.get("name"));
            }else{
                p.put("owner_name","");
            }
            allList.add(p);
        }
        Json json = new Json();
        json.setMsg("获取数据成功。");
        json.setData(allList);
        json.setSuccess(true);
        this.writeJson(response,json);
    }


    @PostMapping("/completeCheck")
    public void completeCheck(@Valid @RequestBody CheckTask data, HttpServletResponse response) throws Exception {
        Json json = new Json();
        String userParams = SecurityContextHolder.getContext().getAuthentication().getName();
        String user_id = userParams.split(":")[1];
        try {
            //处理流程
            boolean assignmentUser = true;
            String businessKey = data.getBusinessKey();
            Task currentTask = taskService.createTaskQuery().taskId(data.getId()).singleResult();
            //添加评论
            if (Verify.verifyIsNotNull(data.getApprove_opinion())) {
                taskService.addComment(currentTask.getId(), currentTask.getProcessInstanceId(), data.getApprove_opinion());
            }
            // 被委派人处理完成任务
            if (Verify.verifyIsNotNull(currentTask.getOwner())) {
                taskService.resolveTask(currentTask.getId(), PageData.getInstance().objToMap(data));
            }
            //判断是不是多实例任务
            Map map = PageData.getInstance().objToMap(data);
            data.getNodeData().forEach(item -> {
                if(Verify.verifyIsNotNull(item.get("multiInstances"))&&item.get("multiInstances").toString().contains("true")){
                    String[] flag = item.get("multiInstances").toString().split(":");
                    map.put(flag[0], Arrays.asList(item.get("deal_ids").toString().split(",")));
                }
            });
            taskService.complete(currentTask.getId(), map);

            BpmnModel bpmnModel = repositoryService.getBpmnModel(currentTask.getProcessDefinitionId());
            //获取当前活动对象
            FlowElement flowElement = bpmnModel.getFlowElement(currentTask.getTaskDefinitionKey());
            // 根据活动id获取活动实例
            if (flowElement instanceof UserTask) {
                UserTask userTask = (UserTask) flowElement;
                if (userTask.getBehavior() instanceof ParallelMultiInstanceBehavior) {
                    assignmentUser = false;
                } else if (userTask.getBehavior() instanceof SequentialMultiInstanceBehavior) {
                    assignmentUser = false;
                }
            }
            //如果当前节点是多实例任务，则不指定下一环节办理人
            //处理流程上报-指定下一任务办理人-排除多实例
            if (assignmentUser) {
                List<Task> list = taskService.createTaskQuery().processInstanceId(data.getProcessInstanceId()).processInstanceBusinessKey(businessKey).orderByTaskCreateTime().desc().list();
                for (Task task : list) {
                    //根据流程定义id获取bpmnModel对象
                    bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
                    //获取当前节点信息
                    FlowNode flowNode = (FlowNode) bpmnModel.getFlowElement(task.getTaskDefinitionKey());
                    if (flowNode instanceof UserTask) {
                        //判断多实例
                        UserTask userTask = (UserTask) flowNode;
                        if (!(userTask.getBehavior() instanceof ParallelMultiInstanceBehavior) && !(userTask.getBehavior() instanceof SequentialMultiInstanceBehavior)) {//不是多实例需要设置下一节点办理人
                            data.getNodeData().forEach(item -> {
                                if (item.get("node_id").equals(flowNode.getId())) {//设置代办人
                                    if (item.get("assign_mode").equals("10")) {
                                        String[] uids = item.get("deal_ids").toString().split(",");
                                        for (int j = 0; j < uids.length; j++) {
                                            taskService.addCandidateUser(task.getId(), uids[j]);
                                        }
                                    } else if (item.get("assign_mode").equals("11")) {
                                        String[] uids = item.get("deal_ids").toString().split(",");
                                        for (int j = 0; j < uids.length; j++) {
                                            taskService.addCandidateGroup(task.getId(), uids[j]);
                                        }
                                    } else {
                                        String[] uids = item.get("deal_ids").toString().split(",");
                                        for (int j = 0; j < uids.length; j++) {
                                            taskService.setAssignee(task.getId(), uids[j]);
                                        }
                                    }
                                }
                            });
                        }
                    }
                }
            }else{
                List<Task> list = taskService.createTaskQuery().processInstanceId(data.getProcessInstanceId()).processInstanceBusinessKey(businessKey).orderByTaskCreateTime().desc().list();
                for (Task task : list) {
                    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
                    //根据流程定义id获取bpmnModel对象
                    bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
                    //获取当前节点信息
                    FlowNode flowNode = (FlowNode) bpmnModel.getFlowElement(task.getTaskDefinitionKey());
                    if (flowNode instanceof UserTask) {
                        data.getNodeData().forEach(item -> {
                            if (item.get("node_id").equals(flowNode.getId())&&item.get("type").equals("1")) {//设置代办人
                                //判断多实例
                                UserTask userTask = (UserTask) flowNode;
                                if (!(userTask.getBehavior() instanceof ParallelMultiInstanceBehavior) && !(userTask.getBehavior() instanceof SequentialMultiInstanceBehavior)) {//不是多实例需要设置下一节点办理人
                                    String deal_ids = item.get("deal_ids").toString();
                                    if (item.get("assign_mode").equals("10")) {
                                        String[] userIds = deal_ids.split(",");
                                        for (int i = 0; i < userIds.length; i++) {
                                            if (Verify.verifyIsNotNull(userIds[i])) {
                                                taskService.addCandidateUser(task.getId(), userIds[i]);
                                            }
                                        }
                                        if (item.get("assign_mode").equals("11")) {
                                            String[] organizeIds = deal_ids.split(",");
                                            for (int i = 0; i < organizeIds.length; i++) {
                                                if (Verify.verifyIsNotNull(organizeIds[i])) {
                                                    taskService.addCandidateGroup(task.getId(), organizeIds[i]);
                                                }
                                            }
                                        } else {
                                            if (Verify.verifyIsNotNull(deal_ids)) {
                                                taskService.setAssignee(task.getId(), deal_ids);
                                            }
                                        }
                                    }
                                }
                            }
                        });
                    }
                }
            }
            json.setSuccess(true);
            json.setMsg("审核成功");
        } catch (Exception e) {
            String message = "审核失败";
            json.setSuccess(false);
            json.setMsg(message);
            log.error(message, e);
            throw new MyException(message);
        }
        this.writeJson(response,json);
    }


    /**
     * @title getRunNodes
     * @description 获取已经执行的任务节点
     * @author Administrator
     * @updateTime 2021/9/20 0020 23:18
     */
    @GetMapping("/getRunNodes")
    public void getRunNodes(HttpServletRequest request,HttpServletResponse response) throws Exception {
        PageData pd = new PageData(request);
        List<PageData> list = new ArrayList<PageData>();
        Task task = taskService.createTaskQuery()
                .taskId(pd.get("taskId").toString())
                .singleResult();
        // 获取流程历史中已执行节点，并按照节点在流程中执行先后顺序排序
        List<HistoricActivityInstance> historicActivityInstanceList = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(task.getProcessInstanceId())
                .activityType("userTask")   //用户任务
                .finished()       //已经执行的任务节点
                .orderByHistoricActivityInstanceEndTime()
                .asc()
                .list();

        // 已执行的节点ID集合
        if(Verify.verifyIsNotNull(historicActivityInstanceList)){
            // map = historicActivityInstanceList.stream().collect(Collectors.toMap(HistoricActivityInstance::getActivityId,HistoricActivityInstance::getActivityName,(k1,k2)->k1));
            for (HistoricActivityInstance historicActivityInstance:historicActivityInstanceList){
                System.out.println(historicActivityInstance.getActivityId()+"#"+historicActivityInstance.getActivityName());
                PageData p= new PageData();
                p.put(historicActivityInstance.getActivityId(),historicActivityInstance.getActivityName());
                p.put("activityId",historicActivityInstance.getActivityId());
                p.put("activityName",historicActivityInstance.getActivityName());
                list.add(p);
            }
        }
        //去重
        List<PageData> myList = list.stream().distinct().collect(Collectors.toList());
        Json json = new Json();
        json.setSuccess(true);
        json.setData(myList);
        json.setMsg("获取数据成功。");
        this.writeJson(response,json);
    }









    @PostMapping("/findNextAssignmentBak")
    public void findNextAssignmentBak(@RequestBody PageData pd,HttpServletRequest request,HttpServletResponse response) throws Exception {
        List<PageData> list = new ArrayList<PageData>();
        //获取当前节点信息
        FlowNode flowNode = null;
        if(pd.get("process_status").equals("0")){//未启动
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(pd.getString("processDefinitionKey")).latestVersion().singleResult();
            //根据流程定义id获取bpmnModel对象
            BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinition.getId());
            pd.put("model_id",pd.getString("processDefinitionKey"));

            //Process对象封装了全部的节点、连线、以及关口等信息。拿到这个对象就能够为所欲为了。
            Process process = bpmnModel.getProcesses().get(0);
            //获取全部的FlowElement（流元素）信息
            Collection<FlowElement> flowElements = process.getFlowElements();
            for (FlowElement flowElement : flowElements) {
                //开始节点
                if(flowElement instanceof StartEvent) {
                    flowNode = (FlowNode) flowElement;
                }
            }
//            String flowElementId = activitiParamConfig.getStartFlowElementId();
//            System.out.println("###:"+flowElementId);
//            flowNode = (FlowNode) bpmnModel.getFlowElement(flowElementId);
            System.out.println(flowNode);
        }else{//已启动
            Task task = taskService.createTaskQuery().taskId(pd.get("taskId").toString()).singleResult();
            pd.put("model_id",pd.getString("processDefinitionKey"));
            //根据流程定义id获取bpmnModel对象
            BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
            //获取当前节点信息
            flowNode = (FlowNode) bpmnModel.getFlowElement(task.getTaskDefinitionKey());
        }
        //获取当前节点输出连线
        List<SequenceFlow> outgoingFlows = flowNode.getOutgoingFlows();
        //遍历输出连线
        for (SequenceFlow outgoingFlow : outgoingFlows) {
            //获取输出节点元素
            FlowElement targetFlowElement = outgoingFlow.getTargetFlowElement();
            //排除非用户任务接点
            if(targetFlowElement instanceof UserTask){
                //获取输出节点id==名称
//				System.out.println(outgoingFlow.getTargetFlowElement().getId()+"======="
//						+outgoingFlow.getTargetFlowElement().getName());
                PageData p = new PageData();
                p.put("node_id",outgoingFlow.getTargetFlowElement().getId());
                p.put("node_name",outgoingFlow.getTargetFlowElement().getName());
                //查询人员指定
                pd.put("node_key",outgoingFlow.getTargetFlowElement().getId());
                PageData assignPd = assignmentService.findActivitiAssignment(pd);
                p.put("assignPd",assignPd);

                //判断是否是多实例任务
                String flag = "";
                UserTask userTask = (UserTask) targetFlowElement;
                if (userTask.getBehavior() instanceof ParallelMultiInstanceBehavior) {
                    ParallelMultiInstanceBehavior behavior = (ParallelMultiInstanceBehavior) userTask.getBehavior();
//					if (behavior != null && behavior.getCollectionExpression() != null) {//验证表达式
                    flag = behavior.getCollectionVariable()+":true";

//					}
                }else if (userTask.getBehavior() instanceof SequentialMultiInstanceBehavior) {
                    SequentialMultiInstanceBehavior behavior = (SequentialMultiInstanceBehavior) userTask.getBehavior();
                    flag = behavior.getCollectionVariable()+":true";
                }
                p.put("multiInstances",flag);
                list.add(p);
            }else if(targetFlowElement instanceof InclusiveGateway){//包含网关
                //包含网关
                InclusiveGateway gateway = ((InclusiveGateway) targetFlowElement);
                List<SequenceFlow> tmpList = gateway.getOutgoingFlows();//网关后面的连线
                for (SequenceFlow sf2 : tmpList) {//遍历这些连线 并查询到相关的userTask
                    if(StringUtils.isEmpty(sf2.getConditionExpression())){//SequenceFlow上如果没有conditionExpression 表达式，直接处理用户任务
                        FlowElement targetRef2 = sf2.getTargetFlowElement();
                        if(targetRef2 instanceof UserTask){
                            PageData p = new PageData();
                            p.put("node_id",targetRef2.getId());
                            p.put("node_name",targetRef2.getName());
                            //查询人员指定
                            pd.put("node_key",targetRef2.getId());
                            PageData assignPd = assignmentService.findActivitiAssignment(pd);
                            p.put("assignPd",assignPd);
                            //判断是否是多实例任务
                            String flag = "";
                            UserTask userTask = (UserTask) targetRef2;
                            if (userTask.getBehavior() instanceof ParallelMultiInstanceBehavior) {
                                ParallelMultiInstanceBehavior behavior = (ParallelMultiInstanceBehavior) userTask.getBehavior();
//								if (behavior != null && behavior.getCollectionExpression() != null) {
                                flag = behavior.getCollectionVariable()+":true";
//								}
                            }else if (userTask.getBehavior() instanceof SequentialMultiInstanceBehavior) {
                                SequentialMultiInstanceBehavior behavior = (SequentialMultiInstanceBehavior) userTask.getBehavior();
                                flag = behavior.getCollectionVariable()+":true";
                            }
                            p.put("multiInstances",flag);
                            list.add(p);
                        }
                    }else{
                        Serializable compiled = MVEL.compileExpression(sf2.getConditionExpression().replace("${","").replace("}",""));
                        Boolean result = MVEL.executeExpression(compiled, pd, Boolean.class);//判断是否会走当前节点
                        if(result){//符合条件
                            FlowElement targetRef2 = sf2.getTargetFlowElement();
                            if(targetRef2 instanceof UserTask){
                                PageData p = new PageData();
                                p.put("node_id",targetRef2.getId());
                                p.put("node_name",targetRef2.getName());
                                //查询人员指定
                                pd.put("node_key",targetRef2.getId());
                                PageData assignPd = assignmentService.findActivitiAssignment(pd);
                                p.put("assignPd",assignPd);

                                //判断是否是多实例任务
                                String flag = "";
                                UserTask userTask = (UserTask) targetRef2;
                                if (userTask.getBehavior() instanceof ParallelMultiInstanceBehavior) {
                                    ParallelMultiInstanceBehavior behavior = (ParallelMultiInstanceBehavior) userTask.getBehavior();
//									if (behavior != null && behavior.getCollectionExpression() != null) {
                                    flag = behavior.getCollectionVariable()+":true";
//									}
                                }else if (userTask.getBehavior() instanceof SequentialMultiInstanceBehavior) {
                                    SequentialMultiInstanceBehavior behavior = (SequentialMultiInstanceBehavior) userTask.getBehavior();
                                    flag = behavior.getCollectionVariable()+":true";
                                }
                                p.put("multiInstances",flag);
                                list.add(p);
                            }
                        }
                    }
                }

            }else if(targetFlowElement instanceof ParallelGateway){//并行网关
                //包含网关
                ParallelGateway gateway = ((ParallelGateway) targetFlowElement);
                List<SequenceFlow> tmpList = gateway.getOutgoingFlows();//网关后面的连线
                for (SequenceFlow sf2 : tmpList) {//遍历这些连线 并查询到相关的userTask
                    FlowElement targetRef2 = sf2.getTargetFlowElement();
                    if(targetRef2 instanceof UserTask){
                        PageData p = new PageData();
                        p.put("node_id",targetRef2.getId());
                        p.put("node_name",targetRef2.getName());
                        //查询人员指定
                        pd.put("node_key",targetRef2.getId());
                        PageData assignPd = assignmentService.findActivitiAssignment(pd);
                        p.put("assignPd",assignPd);

                        //判断是否是多实例任务
                        String flag = "";
                        UserTask userTask = (UserTask) targetRef2;
                        if (userTask.getBehavior() instanceof ParallelMultiInstanceBehavior) {
                            ParallelMultiInstanceBehavior behavior = (ParallelMultiInstanceBehavior) userTask.getBehavior();
//							if (behavior != null && behavior.getCollectionExpression() != null) {
                            flag = behavior.getCollectionVariable()+":true";
//							}
                        }else if (userTask.getBehavior() instanceof SequentialMultiInstanceBehavior) {
                            SequentialMultiInstanceBehavior behavior = (SequentialMultiInstanceBehavior) userTask.getBehavior();
                            flag = behavior.getCollectionVariable()+":true";
                        }
                        p.put("multiInstances",flag);
                        list.add(p);
                    }
                }
            }else if(targetFlowElement instanceof ExclusiveGateway){//排他网关
                int i = 0;//用户判断多条满足的情况
                //网关
                ExclusiveGateway gateway = ((ExclusiveGateway) targetFlowElement);
                List<SequenceFlow> tmpList = gateway.getOutgoingFlows();//网关后面的连线
                for (SequenceFlow sf2 : tmpList) {//遍历这些连线 并查询到相关的userTask
                    if(StringUtils.isEmpty(sf2.getConditionExpression())){//SequenceFlow上如果没有conditionExpression 就不去关注
                        continue;
                    }else{
                        Serializable compiled = MVEL.compileExpression(sf2.getConditionExpression().replace("${","").replace("}",""));
                        Boolean result = MVEL.executeExpression(compiled, pd, Boolean.class);//判断是否会走当前节点
                        if(result){//符合条件
                            FlowElement targetRef2 = sf2.getTargetFlowElement();
                            if(i==0){//只取默认第一条。
                                i=1;
                                if(targetRef2 instanceof UserTask){
                                    PageData p = new PageData();
                                    p.put("node_id",targetRef2.getId());
                                    p.put("node_name",targetRef2.getName());
                                    //查询人员指定
                                    pd.put("node_key",targetRef2.getId());
                                    PageData assignPd = assignmentService.findActivitiAssignment(pd);
                                    p.put("assignPd",assignPd);

                                    //判断是否是多实例任务
                                    String flag = "";
                                    UserTask userTask = (UserTask) targetRef2;
                                    if (userTask.getBehavior() instanceof ParallelMultiInstanceBehavior) {
                                        ParallelMultiInstanceBehavior behavior = (ParallelMultiInstanceBehavior) userTask.getBehavior();
//										if (behavior != null && behavior.getCollectionExpression() != null) {
                                        flag = behavior.getCollectionVariable()+":true";
//										}
                                    }else if (userTask.getBehavior() instanceof SequentialMultiInstanceBehavior) {
                                        SequentialMultiInstanceBehavior behavior = (SequentialMultiInstanceBehavior) userTask.getBehavior();
                                        flag = behavior.getCollectionVariable()+":true";
                                    }
                                    p.put("multiInstances",flag);
                                    list.add(p);
                                }
                            }
                        }
                    }
                }

            }
        }

        //查询流程启动人
        boolean flag = false;
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(pd.get("processInstanceId").toString()).singleResult();
        if(Verify.verifyIsNotNull(processInstance)){//已启动流程
            String start_user_id = processInstance.getStartUserId();
            pd.put("user_id",start_user_id.split("_")[1]);
            PageData uPd = userService.findUserInfo(pd);
            pd.put("start_user_id",start_user_id.split("_")[1]);
            pd.put("start_user_name",uPd.get("name"));
            pd.put("start_organize_id",start_user_id.split("_")[0]);

            Task task = taskService.createTaskQuery().taskId(pd.get("taskId").toString()).singleResult();
            BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
            //获取当前活动对象
            FlowElement flowElement = bpmnModel.getFlowElement(task.getTaskDefinitionKey());
            // 根据活动id获取活动实例
            if (flowElement instanceof UserTask) {
                UserTask userTask = (UserTask) flowElement;
                if (userTask.getBehavior() instanceof ParallelMultiInstanceBehavior) {
                    flag = true;
                }else if(userTask.getBehavior() instanceof SequentialMultiInstanceBehavior){
                    flag = true;
                }
            }
        }
        pd.put("node_flag",flag);
        Json json = new Json();
        json.setData(list);
        json.setObject(pd);
        json.setSuccess(true);
        json.setMsg("数据获取成功。");
        this.writeJson(response,json);
    }


    /**
     * @title findFormParams
     * @description 查询表单参数
     * @author Administrator
     * @updateTime 2022/5/9 0009 21:42
     */
    @PostMapping("/findFormParams")
    public void findFormParams(@RequestBody PageData pd,HttpServletRequest request,HttpServletResponse response) throws Exception {
        List<PageData> list = new ArrayList<PageData>();
        //获取当前节点信息
        BpmnModel bpmnModel = null;
        if (pd.get("nodeType").equals("startNode")) {//未启动
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(pd.getString("processDefinitionKey")).latestVersion().singleResult();
            //根据流程定义id获取bpmnModel对象
            bpmnModel = repositoryService.getBpmnModel(processDefinition.getId());
            //创建转换对象
            BpmnXMLConverter converter = new BpmnXMLConverter();
            //把bpmnModel对象转换成字符
            byte[] bytes = converter.convertToXML(bpmnModel);
            String xmlContenxt = new String(bytes);
            System.out.println(xmlContenxt);
            Document document = XmlUtil.readXML(xmlContenxt);
            NodeList node = XmlUtil.getNodeListByXPath("//*[name()='findFormParams']", document);
            String formKey = "";
            String writeFieldKey = "";
            String hideFieldKey = "";
            for (int i = 0; i < node.getLength(); i++) {
                if(Verify.verifyIsNotNull(node.item(i).getAttributes().getNamedItem("activiti:formKey"))){
                    formKey = node.item(i).getAttributes().getNamedItem("activiti:formKey").getNodeValue();
                }
//                System.out.println(node.item(i).getNodeName() + "---" + node.item(i).getAttributes().getNamedItem("id") + "---" + node.item(i).getAttributes().getNamedItem("name") + "---" + node.item(i).getAttributes().getNamedItem("activiti:formKey").getNodeValue());
                NodeList childList = node.item(i).getChildNodes();
                for (int j = 0; j < childList.getLength(); j++) {
//                    System.out.println(childList.item(j).getNodeName());
                    if (childList.item(j).getNodeName() != "extensionElements") {
                        continue;
                    }
                    NodeList cls = childList.item(j).getChildNodes();
                    for (int m = 0; m < cls.getLength(); m++) {
                        if (cls.item(m).getNodeName() != "activiti:field") {
                            continue;
                        }
                        if(Verify.verifyIsNotNull(cls.item(m).getAttributes().getNamedItem("writeFieldKey"))){
                            writeFieldKey = cls.item(m).getAttributes().getNamedItem("writeFieldKey").getNodeValue();
                        }
                        if(Verify.verifyIsNotNull(cls.item(m).getAttributes().getNamedItem("hideFieldKey").getNodeValue())){
                            hideFieldKey = cls.item(m).getAttributes().getNamedItem("hideFieldKey").getNodeValue();
                        }
                        System.out.println(cls.item(m).getAttributes().getNamedItem("elementText"));
                    }
                }
            }
            pd.put("formKey",formKey);
            pd.put("writeFieldKey",writeFieldKey);
            pd.put("hideFieldKey",hideFieldKey);
        } else {//已启动
            Task task = taskService.createTaskQuery().taskId(pd.get("taskId").toString()).singleResult();

            pd.put("model_id", task.getProcessDefinitionId().split(":")[0]);
            //根据流程定义id获取bpmnModel对象
            bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());

            //创建转换对象
            BpmnXMLConverter converter = new BpmnXMLConverter();
            //把bpmnModel对象转换成字符
            byte[] bytes = converter.convertToXML(bpmnModel);
            String xmlContenxt = new String(bytes);
            System.out.println(xmlContenxt);
            Document document = XmlUtil.readXML(xmlContenxt);
            NodeList node = XmlUtil.getNodeListByXPath("//*[name()='bpmn2:userTask']", document);

            String formKey = "";
            String writeFieldKey = "";
            String hideFieldKey = "";
            for (int i = 0; i < node.getLength(); i++) {
                System.out.println(task.getTaskDefinitionKey()+":::"+node.item(i).getAttributes().getNamedItem("id").getNodeValue());
                if(task.getTaskDefinitionKey().equals(node.item(i).getAttributes().getNamedItem("id").getNodeValue())){
                    if(Verify.verifyIsNotNull(node.item(i).getAttributes().getNamedItem("activiti:formKey"))){
                        formKey = node.item(i).getAttributes().getNamedItem("activiti:formKey").getNodeValue();
                    }
//                    System.out.println(node.item(i).getNodeName() + "---" + node.item(i).getAttributes().getNamedItem("id") + "---" + node.item(i).getAttributes().getNamedItem("name") + "---" + node.item(i).getAttributes().getNamedItem("activiti:formKey").getNodeValue());
                    NodeList childList = node.item(i).getChildNodes();
                    for (int j = 0; j < childList.getLength(); j++) {
                        if (childList.item(j).getNodeName() != "extensionElements") {
                            continue;
                        }
                        NodeList cls = childList.item(j).getChildNodes();
                        for (int m = 0; m < cls.getLength(); m++) {
                            if (cls.item(m).getNodeName() != "activiti:field") {
                                continue;
                            }
                            if(Verify.verifyIsNotNull(cls.item(m).getAttributes().getNamedItem("writeFieldKey"))){
                                writeFieldKey = cls.item(m).getAttributes().getNamedItem("writeFieldKey").getNodeValue();
                            }
                            if(Verify.verifyIsNotNull(cls.item(m).getAttributes().getNamedItem("hideFieldKey"))){
                                hideFieldKey = cls.item(m).getAttributes().getNamedItem("hideFieldKey").getNodeValue();
                            }
//                        System.out.println(cls.item(m).getAttributes().getNamedItem("elementText"));
                        }
                    }
                }
            }
            pd.put("formKey",formKey);
            pd.put("writeFieldKey",writeFieldKey);
            pd.put("hideFieldKey",hideFieldKey);
        }
        Json json = new Json();
        json.setData(pd);
        json.setSuccess(true);
        json.setMsg("数据获取成功。");
        this.writeJson(response,json);
    }


}
