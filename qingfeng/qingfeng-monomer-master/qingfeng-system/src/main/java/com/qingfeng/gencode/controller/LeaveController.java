package com.qingfeng.gencode.controller;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.qingfeng.activiti.service.IAssignmentService;
import com.qingfeng.base.controller.BaseController;
import com.qingfeng.entity.MyResponse;
import com.qingfeng.entity.QueryRequest;
import com.qingfeng.entity.gencode.Leave;
import com.qingfeng.entity.system.UserOrganize;
import com.qingfeng.exception.MyException;
import com.qingfeng.gencode.service.ILeaveService;
import com.qingfeng.system.service.IGroupService;
import com.qingfeng.system.service.IUserOrganizeService;
import com.qingfeng.system.service.IUserService;
import com.qingfeng.utils.*;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.sf.jxls.transformer.XLSTransformer;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.bpmn.behavior.ParallelMultiInstanceBehavior;
import org.activiti.engine.impl.bpmn.behavior.SequentialMultiInstanceBehavior;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.repository.Model;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qingfeng.utils.upload.ParaUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.File;
import java.util.*;


/**
* @title: LeaveController
* @projectName: LeaveController
* @description: TODO
* @author: qingfeng
* @createTime 2021/4/3 0003 20:30
*/
@Slf4j
@Validated
@RestController
@RequestMapping("/gencode/leave")
public class LeaveController extends BaseController {

    @Autowired
    private ILeaveService leaveService;
    @Autowired
    private IUserOrganizeService userOrganizeService;
    @Autowired
    private IUserService userService;
    //activiti 工作流 service
    @Autowired
    private TaskService taskService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private IAssignmentService assignmentService;
    @Autowired
    private IGroupService groupService;
    //流程定义KEY
    public static final String process_key = "Process_1631373997339";

    /**
    * @title findListPage
    * @description 查询数据列表
    * @author qingfeng
    * @updateTime 2021/4/3 0003 20:30
    */
    @GetMapping("/findListPage")
    @PreAuthorize("hasAnyAuthority('leave:info')")
    public MyResponse findListPage(QueryRequest queryRequest, Leave leave) throws Exception {
        PageData pd = new PageData();

        String userParams = SecurityContextHolder.getContext().getAuthentication().getName();
        //处理数据权限
        String user_id = userParams.split(":")[1];
        UserOrganize uoParam = new UserOrganize();
        uoParam.setUser_id(user_id);
        UserOrganize userOrganize = userOrganizeService.findUserOrganizeInfo(uoParam);
        List<String> auth_organize_ids = new ArrayList<String>();
        if(Verify.verifyIsNotNull(userOrganize)){
            if(Verify.verifyIsNotNull(userOrganize.getAuthOrgIds())){
                auth_organize_ids = Arrays.asList(userOrganize.getAuthOrgIds().split(","));
            }
        }
        leave.setAuth_user(user_id);
        leave.setAuth_organize_ids(auth_organize_ids);
        IPage<Leave> list = leaveService.findListPage(leave, queryRequest);
        for (Leave myLeave: list.getRecords()) {
            myLeave.setProcess_key(process_key);
            String businessKey = "business:gencode:leave:"+myLeave.getId();

            List<Task> taskList = taskService.createTaskQuery().processInstanceBusinessKeyLike(businessKey).orderByTaskCreateTime().desc().list();
            if(Verify.verifyIsNotNull(taskList)&&taskList.size()>0){
                StringBuffer blr = new StringBuffer();
                StringBuffer blzt = new StringBuffer();
                for (Task task:taskList) {
                    if(Verify.verifyIsNotNull(task.getAssignee())){
                        pd.put("user_id",task.getAssignee());
                        PageData userPd = userService.findUserInfo(pd);
                        blr.append(userPd.get("name")).append(",");
                    }else{
                        blr.append("任务未认领").append(",");
                    }
                    blzt.append(task.getName()).append(",");
                }
                if(blr.length()>0){
                    myLeave.setAssignee(blr.substring(0,blr.length()-1));
                }
                if(blzt.length()>0){
                    myLeave.setDealStatus(blzt.substring(0,blzt.length()-1));
                }
                myLeave.setProcessStatus("1");
                myLeave.setDealTime(DateTimeUtil.getDateTimeStr(taskList.get(0).getCreateTime()));
            }else{
                List<HistoricProcessInstance> piList = historyService.createHistoricProcessInstanceQuery().processInstanceBusinessKey(businessKey).list();
                if(piList.size()>0){
                    myLeave.setDealStatus("流程已结束");
                    myLeave.setProcessStatus("2");
                }else{
                    myLeave.setDealStatus("流程未启动");
                    myLeave.setProcessStatus("0");
                }
            }
            if(!myLeave.getProcessStatus().equals("0")){
                ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(businessKey).singleResult();
                myLeave.setBusinessKey(businessKey);
                if(Verify.verifyIsNotNull(processInstance)){
                    myLeave.setProcessInstanceId(processInstance.getId());
                }
            }
        }

        Map<String, Object> dataTable = MyUtil.getDataTable(list);
        MyResponse myResponse = new MyResponse();
        myResponse.data(dataTable);
        myResponse.put("process_key",process_key);
        return myResponse;
    }

    /**
    * @title findList
    * @description 查询列表
    * @author qingfeng
    * @updateTime 2021/4/3 0003 20:31
    */
    @GetMapping("/findList")
    public MyResponse findList(QueryRequest queryRequest, Leave leave) {
        String userParams = SecurityContextHolder.getContext().getAuthentication().getName();
        //处理数据权限
        String user_id = userParams.split(":")[1];
        UserOrganize uoParam = new UserOrganize();
        uoParam.setUser_id(user_id);
        UserOrganize userOrganize = userOrganizeService.findUserOrganizeInfo(uoParam);
        List<String> auth_organize_ids = new ArrayList<String>();
        if(Verify.verifyIsNotNull(userOrganize)){
            if(Verify.verifyIsNotNull(userOrganize.getAuthOrgIds())){
                auth_organize_ids = Arrays.asList(userOrganize.getAuthOrgIds().split(","));
            }
        }
        leave.setAuth_user(user_id);
        leave.setAuth_organize_ids(auth_organize_ids);
        List<Leave> leaveList = leaveService.findList(leave);
        return new MyResponse().data(leaveList);
    }

    /**
     * @title findInfo
     * @description 查询详情列表
     * @author Administrator
     * @updateTime 2021/10/6 0006 16:51
     */
    @GetMapping("/findInfo")
    public MyResponse findInfo(QueryRequest queryRequest, Leave leave) {
        String userParams = SecurityContextHolder.getContext().getAuthentication().getName();
        Leave leave1 = leaveService.getById(leave.getId());
        return new MyResponse().data(leave1);
    }

    /**
    * @title save
    * @description 保存方法
    * @author qingfeng
    * @updateTime 2021/4/3 0003 20:31
    */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('leave:add')")
    public void save(@Valid @RequestBody Leave leave,HttpServletResponse response) throws Exception {
        Json json = new Json();
        try {
            this.leaveService.saveLeave(leave);
            //流程提报
            if(leave.getSubmitType().equals("2")){
                //处理流程上报
                String businessKey = "business:gencode:leave:"+leave.getId();
                //获取当前用户
                String userParams = SecurityContextHolder.getContext().getAuthentication().getName();
                Authentication.setAuthenticatedUserId(userParams.split(":")[2]+"_"+userParams.split(":")[1]);

                runtimeService.startProcessInstanceByKey(process_key,businessKey,PageData.getInstance().objToMap(leave));
                System.out.println("============================");
                System.out.println("流程启动成功");
                //当前任务
                List<Task> list = taskService.createTaskQuery().processDefinitionKey(process_key).processInstanceBusinessKey(businessKey).orderByTaskCreateTime().desc().list();
                for (Task task:list) {
                    //根据流程定义id获取bpmnModel对象
                    BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
                    //获取当前节点信息
                    FlowNode flowNode = (FlowNode) bpmnModel.getFlowElement(task.getTaskDefinitionKey());
                    if(flowNode instanceof UserTask){
                        leave.getNodeData().forEach(item -> {
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
                                    }else if (item.get("assign_mode").equals("11")) {
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
                        });
                    }
                }
            }
            json.setSuccess(true);
            json.setMsg("新增信息成功");
        } catch (Exception e) {
            String message = "新增信息失败";
            json.setSuccess(false);
            json.setMsg(message);
            log.error(message, e);
            throw new MyException(message);
        }
        this.writeJson(response,json);
    }

    /**
    * @title update
    * @description 更新方法
    * @author qingfeng
    * @updateTime 2021/4/3 0003 20:31
    */
    @PutMapping
    @PreAuthorize("hasAnyAuthority('leave:edit')")
    public void update(@Valid @RequestBody Leave leave,HttpServletResponse response) throws Exception {
        Json json = new Json();
        try {
            this.leaveService.updateLeave(leave);

            PageData pd = new PageData();
            String userParams = SecurityContextHolder.getContext().getAuthentication().getName();
            String user_id = userParams.split(":")[1];
            String organize_id = userParams.split(":")[2];
            //查询当前登录用户信息
            pd.put("user_id",user_id);
            PageData user = userService.findUserInfo(pd);
            if(leave.getSubmitType().equals("2")||leave.getSubmitType().equals("3")){
                //处理流程
                boolean assignmentUser = true;
                String currentNodeId = "";
                String businessKey = "business:gencode:leave:"+leave.getId();
                if (leave.getSubmitType().equals("2")) {//发起流程
                    //获取当前用户
                    Authentication.setAuthenticatedUserId(organize_id+"_"+user_id);
                    runtimeService.startProcessInstanceByKey(process_key, businessKey, PageData.getInstance().objToMap(leave));
                } else if (leave.getSubmitType().equals("3")) {//审批
                    Task currentTask = taskService.createTaskQuery().taskId(leave.getTaskId()).singleResult();
                    //添加评论
                    if (Verify.verifyIsNotNull(leave.getApprove_opinion())) {
                        taskService.addComment(leave.getTaskId(), currentTask.getProcessInstanceId(), leave.getApprove_opinion());
                    }
                    // 被委派人处理完成任务
                    if (Verify.verifyIsNotNull(currentTask.getOwner())) {
                        taskService.resolveTask(leave.getTaskId(), PageData.getInstance().objToMap(leave));
                    }
                    //判断是不是多实例任务
                    Map map = PageData.getInstance().objToMap(leave);
                    leave.getNodeData().forEach(item -> {
                        if(Verify.verifyIsNotNull(item.get("multiInstances"))&&item.get("multiInstances").toString().contains("true")){
                            String[] flag = item.get("multiInstances").toString().split(":");
                            map.put(flag[0], Arrays.asList(item.get("deal_ids").toString().split(",")));
                        }
                    });
                    taskService.complete(leave.getTaskId(), map);

                    BpmnModel bpmnModel = repositoryService.getBpmnModel(currentTask.getProcessDefinitionId());
                    //获取当前活动对象
                    FlowElement flowElement = bpmnModel.getFlowElement(currentTask.getTaskDefinitionKey());
                    // 根据活动id获取活动实例
                    if (flowElement instanceof UserTask) {
                        currentNodeId=flowElement.getId();
                        UserTask userTask = (UserTask) flowElement;
                        if (userTask.getBehavior() instanceof ParallelMultiInstanceBehavior) {
                            assignmentUser = false;
                        } else if (userTask.getBehavior() instanceof SequentialMultiInstanceBehavior) {
                            assignmentUser = false;
                        }
                    }
                }
                //如果当前节点是多实例任务，则不指定下一环节办理人
                //处理流程上报-指定下一任务办理人-排除多实例
                if (assignmentUser) {
                    List<Task> list = taskService.createTaskQuery().processDefinitionKey(process_key).processInstanceBusinessKey(businessKey).orderByTaskCreateTime().desc().list();
                    for (Task task : list) {
                        //根据流程定义id获取bpmnModel对象
                        BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
                        //获取当前节点信息
                        FlowNode flowNode = (FlowNode) bpmnModel.getFlowElement(task.getTaskDefinitionKey());
                        if (flowNode instanceof UserTask) {
                            //判断多实例
                            UserTask userTask = (UserTask) flowNode;
                            if (!(userTask.getBehavior() instanceof ParallelMultiInstanceBehavior) && !(userTask.getBehavior() instanceof SequentialMultiInstanceBehavior)) {//不是多实例需要设置下一节点办理人
                                leave.getNodeData().forEach(item -> {
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
                    List<Task> list = taskService.createTaskQuery().processDefinitionKey(process_key).processInstanceBusinessKey(businessKey).orderByTaskCreateTime().desc().list();
                    for (Task task : list) {
                        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
                        //根据流程定义id获取bpmnModel对象
                        BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
                        //获取当前节点信息
                        FlowNode flowNode = (FlowNode) bpmnModel.getFlowElement(task.getTaskDefinitionKey());
                        if (flowNode instanceof UserTask) {
                            leave.getNodeData().forEach(item -> {
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
            }
            json.setSuccess(true);
            json.setMsg("修改信息成功");
        } catch (Exception e) {
            String message = "修改信息失败";
            json.setSuccess(false);
            json.setMsg(message);
            log.error(message, e);
            throw new MyException(message);
        }
        this.writeJson(response,json);
    }

    /**
    * @title delete
    * @description 删除方法
    * @author qingfeng
    * @updateTime 2021/4/3 0003 20:31
    */
    @DeleteMapping("/{ids}")
    @PreAuthorize("hasAnyAuthority('leave:del')")
    public void delete(@NotBlank(message = "{required}") @PathVariable String ids,HttpServletResponse response) throws Exception {
        Json json = new Json();
        try {
            String[] del_ids = ids.split(StringPool.COMMA);
            this.leaveService.removeByIds(Arrays.asList(del_ids));
            json.setSuccess(true);
            json.setMsg("删除信息成功");
        } catch (Exception e) {
            String message = "删除信息失败";
            json.setSuccess(false);
            json.setMsg(message);
            log.error(message, e);
            throw new MyException(message);
        }
        this.writeJson(response,json);
    }

    /**
    * @title updateStatus
    * @description 更新状态
    * @author qingfeng
    * @updateTime 2021/4/3 0003 20:31
    */
    @PostMapping("/updateStatus")
    @PreAuthorize("hasAnyAuthority('leave:status')")
    public void updateStatus(@Valid @RequestBody Leave leave,HttpServletResponse response) throws Exception {
        Json json = new Json();
        try {
            this.leaveService.updateById(leave);
            json.setSuccess(true);
            json.setMsg("状态修改成功");
        } catch (Exception e) {
            String message = "状态修改失败";
            json.setSuccess(false);
            json.setMsg(message);
            log.error(message, e);
            throw new MyException(message);
        }
        this.writeJson(response,json);
    }

}
