package com.qingfeng.customize.service.impl;

import cn.hutool.core.util.ArrayUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qingfeng.activiti.service.IAssignmentService;
import com.qingfeng.activiti.service.impl.ProcessServiceImpl;
import com.qingfeng.customize.mapper.VDataMapper;
import com.qingfeng.customize.service.IVDataService;
import com.qingfeng.customize.service.IVfieldService;
import com.qingfeng.customize.service.IVformService;
import com.qingfeng.customize.service.IVmenuService;
import com.qingfeng.entity.QueryRequest;
import com.qingfeng.entity.customize.Vfield;
import com.qingfeng.entity.customize.Vform;
import com.qingfeng.entity.customize.Vmenu;
import com.qingfeng.system.service.IGroupService;
import com.qingfeng.system.service.IUserService;
import com.qingfeng.utils.*;
import net.sf.json.util.JSONUtils;
import org.activiti.bpmn.model.*;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.bpmn.behavior.ParallelMultiInstanceBehavior;
import org.activiti.engine.impl.bpmn.behavior.SequentialMultiInstanceBehavior;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.repository.Model;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * @ProjectName VDataServiceImpl
 * @author Administrator
 * @version 1.0.0
 * @Description 数据对象处理
 * @createTime 2021/10/1 0001 22:23
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class VDataServiceImpl extends ServiceImpl<VDataMapper, PageData> implements IVDataService {

    @Autowired
    private IVmenuService vmenuService;
    @Autowired
    private IVformService vformService;
    @Autowired
    private IVfieldService vfieldService;
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
    private ManagementService managementService;
    @Autowired
    private IAssignmentService assignmentService;
    @Autowired
    private IGroupService groupService;

    /**
     * @title findListPage
     * @description 查询数据分页列表
     * @author Administrator
     * @updateTime 2021/10/1 0001 22:22
     */
    public IPage<PageData> findListPage(PageData pd, QueryRequest request){
        Page<PageData> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.baseMapper.findListPage(page, pd);
    }

    /**
     * @title findList
     * @description 查询数据列表
     * @author Administrator
     * @updateTime 2021/10/1 0001 22:22
     */
    public List<PageData> findList(PageData pd){
        return this.baseMapper.findList(pd);
    }

    /**
     * @title saveVData
     * @description 保存对象信息
     * @author Administrator
     * @updateTime 2021/10/1 0001 22:22
     */
    @Transactional
    public void saveVData(PageData pd){
        Vform vform = vformService.getById(pd.get("table_id").toString());
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("table_id",vform.getId());
        List<Vfield> vfields = vfieldService.list(queryWrapper);

        StringBuffer paramKeyField = new StringBuffer();
        StringBuffer paramValueField = new StringBuffer();
        for (Vfield vfield : vfields) {
            if(!vfield.getField_type().equals("batch")){
                paramKeyField.append(vfield.getField_name()).append(",");
                paramValueField.append("#{"+vfield.getField_name()+"}").append(",");
            }
            //处理图片
            if(vfield.getField_type().equals("uploadFile")||vfield.getField_type().equals("uploadImg")){
                List<PageData> list = (List<PageData>)pd.get(vfield.getField_name());
                pd.put(vfield.getField_name(), JsonToMap.list2json(list));
            }
        }
        String id = GuidUtil.getUuid();
        String time = DateTimeUtil.getDateTimeStr();
        String authParams = SecurityContextHolder.getContext().getAuthentication().getName();
        pd.put("id",id);
        pd.put("type","0");
        pd.put("status","0");
        pd.put("create_time",time);
        pd.put("create_user",authParams.split(":")[1]);
        pd.put("create_organize",authParams.split(":")[2]);
        pd.put("param_key", paramKeyField);
        pd.put("param_value", paramValueField);
        pd.put("table_name", vform.getTable_name());
        this.baseMapper.saveData(pd);
        //处理子表
        QueryWrapper formWrapper = new QueryWrapper();
        formWrapper.eq("main_id",pd.get("table_id").toString());
        List<Vform> vforms = vformService.list(formWrapper);
        for (Vform form:vforms) {
            QueryWrapper fieldWrapper = new QueryWrapper();
            fieldWrapper.eq("table_id",form.getId());
            List<Vfield> fields = vfieldService.list(fieldWrapper);
            StringBuffer field_k = new StringBuffer();
            StringBuffer field_v = new StringBuffer();
            List<String> uploadFiledName = new ArrayList<String>();
            for (Vfield field : fields) {
                field_k.append(field.getField_name()).append(",");
                field_v.append("#{"+field.getField_name()+"}").append(",");
                //处理图片
                if(field.getField_type().equals("uploadFile")||field.getField_type().equals("uploadImg")){
                    uploadFiledName.add(field.getField_name());
                }
            }

            List<Map> dataList = (List<Map>)pd.get(form.getTable_name());
            for (Map map:dataList) {
                PageData p = PageData.getInstance().map2Pd(map);
                uploadFiledName.forEach(field_name->{
                    List<PageData> list = (List<PageData>)p.get(field_name);
                    p.put(field_name, JsonToMap.list2json(list));
                });
                p.put("main_id",id);
                p.put("table_name", form.getTable_name());
                p.put("param_key", field_k);
                p.put("param_value", field_v);
                p.put("id", GuidUtil.getUuid());
                p.put("type","0");
                p.put("status","0");
                p.put("create_time",time);
                p.put("create_user",authParams.split(":")[1]);
                p.put("create_organize",authParams.split(":")[2]);
                p.put("menu_id",pd.get("menu_id"));
                this.baseMapper.saveData(p);
            }
        }
        //流程提报
        Vmenu vmenu = vmenuService.getById(pd.get("menu_id").toString());
        if(vmenu.getOpen_process().equals("0")){
            String process_key = vmenu.getProcess_id();
            if(pd.get("submitType").equals("2")){
                //处理流程上报
                String businessKey = "business:customize:"+pd.get("menu_id")+":"+id;
                //获取当前用户
                String userParams = SecurityContextHolder.getContext().getAuthentication().getName();
                Authentication.setAuthenticatedUserId(userParams.split(":")[2]+"_"+userParams.split(":")[1]);

                //判断是不是多实例任务
                List<Map> nodeData = (List<Map>)pd.get("nodeData");
                nodeData.forEach(item -> {
                    if (Verify.verifyIsNotNull(item.get("multiInstances")) && item.get("multiInstances").toString().contains("true")) {
                        String[] flag = item.get("multiInstances").toString().split(":");
                        pd.put("assigneeList", Arrays.asList(item.get("deal_ids").toString().split(",")));
                    }
                });
                runtimeService.startProcessInstanceByKey(process_key,businessKey,pd);
                //当前任务
                List<Task> list = taskService.createTaskQuery().processDefinitionKey(process_key).processInstanceBusinessKey(businessKey).orderByTaskCreateTime().desc().list();
                for (Task task:list) {
                    //根据流程定义id获取bpmnModel对象
                    BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
                    //获取当前节点信息
                    FlowNode flowNode = (FlowNode) bpmnModel.getFlowElement(task.getTaskDefinitionKey());
                    if(flowNode instanceof UserTask){
                        nodeData.forEach(item -> {
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
        }
    }

    /**
     * @title updateVData
     * @description 更新对象信息
     * @author Administrator
     * @updateTime 2021/10/1 0001 22:22
     */
    @Transactional
    public void updateVData(PageData pd){
        Vform vform = vformService.getById(pd.get("table_id").toString());
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("table_id",vform.getId());
        List<Vfield> vfields = vfieldService.list(queryWrapper);
        StringBuffer paramField = new StringBuffer();
        String[] hideFieldKey = pd.get("hideFieldKey").toString().split(",");
        for (Vfield vfield : vfields) {
            if(!vfield.getField_type().equals("batch")){
                int index = ArrayUtil.indexOf(hideFieldKey,vfield.getField_name());
                if(index==-1){
                    paramField.append(vfield.getField_name()+"=#{"+vfield.getField_name()+"}").append(",");
                }
            }
            //处理图片
            if(vfield.getField_type().equals("uploadFile")||vfield.getField_type().equals("uploadImg")){
                List<PageData> list = (List<PageData>)pd.get(vfield.getField_name());
                pd.put(vfield.getField_name(), JsonToMap.list2json(list));
            }
        }
        String id = pd.get("id").toString();
        String time = DateTimeUtil.getDateTimeStr();
        String authParams = SecurityContextHolder.getContext().getAuthentication().getName();
        String user_id = authParams.split(":")[1];
        String organize_id = authParams.split(":")[2];
        pd.put("update_time",time);
        pd.put("update_user",user_id);
        pd.put("paramField", paramField);
        pd.put("table_name", vform.getTable_name());
        this.baseMapper.updateData(pd);
        //处理子表
        QueryWrapper formWrapper = new QueryWrapper();
        formWrapper.eq("main_id",pd.get("table_id").toString());
        List<Vform> vforms = vformService.list(formWrapper);
        for (Vform form:vforms) {
            QueryWrapper fieldWrapper = new QueryWrapper();
            fieldWrapper.eq("table_id",form.getId());
            List<Vfield> fields = vfieldService.list(fieldWrapper);
            StringBuffer childParamField = new StringBuffer();
            StringBuffer field_k = new StringBuffer();
            StringBuffer field_v = new StringBuffer();
            List<String> uploadFiledName = new ArrayList<String>();
            for (Vfield field : fields) {
                int index = ArrayUtil.indexOf(hideFieldKey,field.getField_name());
                if(index==-1){
                    childParamField.append(field.getField_name()+"=#{"+field.getField_name()+"}").append(",");
                    field_k.append(field.getField_name()).append(",");
                    field_v.append("#{"+field.getField_name()+"}").append(",");
                }
                //处理图片
                if(field.getField_type().equals("uploadFile")||field.getField_type().equals("uploadImg")){
                    uploadFiledName.add(field.getField_name());
                }
            }
            List<Map> dataList = (List<Map>)pd.get(form.getTable_name());
            List<String> fieldIds = new ArrayList<String>();
            for (Map map:dataList) {
                PageData p = PageData.getInstance().map2Pd(map);
                uploadFiledName.forEach(field_name->{
                    List<PageData> list = (List<PageData>)p.get(field_name);
                    p.put(field_name, JsonToMap.list2json(list));
                });
                p.put("main_id",id);
                p.put("table_name", form.getTable_name());
                p.put("paramField", childParamField);
                p.put("update_time",time);
                p.put("update_user",user_id);
                if(Verify.verifyIsNotNull(p.get("id"))){
                    fieldIds.add(p.get("id").toString());
                    this.baseMapper.updateData(p);
                }else{
                    p.put("param_key", field_k);
                    p.put("param_value", field_v);
                    p.put("id",GuidUtil.getUuid());
                    p.put("create_time",time);
                    p.put("create_user",user_id);
                    p.put("create_organize",organize_id);
                    p.put("menu_id",pd.get("menu_id"));
                    fieldIds.add(p.get("id").toString());
                    this.baseMapper.saveData(p);
                }
            }
            //删除已经删除的子表数据
            pd.put("fieldIds",fieldIds);
            pd.put("table_name",form.getTable_name());
            this.baseMapper.delMyChildData(pd);
        }
        //流程提报
        Vmenu vmenu = vmenuService.getById(pd.get("menu_id").toString());
        if(vmenu.getOpen_process().equals("0")) {
            String process_key = vmenu.getProcess_id();
            if (pd.get("submitType").equals("2") || pd.get("submitType").equals("3")) {
                //处理流程
                boolean assignmentUser = true;
                String currentNodeId = "";
                String businessKey = "business:customize:"+pd.get("menu_id")+":" + id;
                if (pd.get("submitType").equals("2")) {//发起流程
                    //获取当前用户
                    Authentication.setAuthenticatedUserId(organize_id + "_" + user_id);
                    //判断是不是多实例任务
                    List<Map> nodeData = (List<Map>)pd.get("nodeData");
                    nodeData.forEach(item -> {
                        if (Verify.verifyIsNotNull(item.get("multiInstances")) && item.get("multiInstances").toString().contains("true")) {
                            String[] flag = item.get("multiInstances").toString().split(":");
                            pd.put(flag[0], Arrays.asList(item.get("deal_ids").toString().split(",")));
                        }
                    });
                    runtimeService.startProcessInstanceByKey(process_key, businessKey, pd);
                } else if (pd.get("submitType").equals("3")) {//审批
                    Task currentTask = taskService.createTaskQuery().taskId(pd.get("taskId").toString()).singleResult();
                    //添加评论
                    if (Verify.verifyIsNotNull(pd.get("approve_opinion"))) {
                        taskService.addComment(pd.get("taskId").toString(), currentTask.getProcessInstanceId(), pd.get("approve_opinion").toString());
                    }
                    // 被委派人处理完成任务
                    if (Verify.verifyIsNotNull(currentTask.getOwner())) {
                        taskService.resolveTask(pd.get("taskId").toString(), pd);
                    }
                    //判断是不是多实例任务
                    List<Map> nodeData = (List<Map>)pd.get("nodeData");
                    nodeData.forEach(item -> {
                        if (Verify.verifyIsNotNull(item.get("multiInstances")) && item.get("multiInstances").toString().contains("true")) {
                            String[] flag = item.get("multiInstances").toString().split(":");
                            pd.put(flag[0], Arrays.asList(item.get("deal_ids").toString().split(",")));
                        }
                    });
                    taskService.complete(pd.get("taskId").toString(), pd);

                    BpmnModel bpmnModel = repositoryService.getBpmnModel(currentTask.getProcessDefinitionId());
                    //获取当前活动对象
                    FlowElement flowElement = bpmnModel.getFlowElement(currentTask.getTaskDefinitionKey());
                    // 根据活动id获取活动实例
                    if (flowElement instanceof UserTask) {
                        currentNodeId = flowElement.getId();
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
                                List<Map> nodeData = (List<Map>)pd.get("nodeData");
                                nodeData.forEach(item -> {
                                    if (item.get("node_id").equals(flowNode.getId())&&Verify.verifyIsNotNull(item.get("assign_mode"))) {//设置代办人
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
                } else {
                    List<Task> list = taskService.createTaskQuery().processDefinitionKey(process_key).processInstanceBusinessKey(businessKey).orderByTaskCreateTime().desc().list();
                    for (Task task : list) {
                        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
                        //根据流程定义id获取bpmnModel对象
                        BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
                        //获取当前节点信息
                        FlowNode flowNode = (FlowNode) bpmnModel.getFlowElement(task.getTaskDefinitionKey());
                        if (flowNode instanceof UserTask) {
                            if(flowNode.getId().equals(currentNodeId)){//还是当前节点。
                            }else {//进行下一节点
                                pd.put("model_id", process_key);
                                pd.put("node_key", flowNode.getId());
                                List<Map> nodeData = (List<Map>) pd.get("nodeData");
                                nodeData.forEach(item -> {
                                    PageData assignPd = assignmentService.findActivitiAssignment(pd);
                                    if (item.get("node_id").equals(flowNode.getId()) && assignPd.get("type").equals("1")&&Verify.verifyIsNotNull(item.get("assign_mode"))) {//设置代办人
                                        //判断多实例
                                        UserTask userTask = (UserTask) flowNode;
                                        if (!(userTask.getBehavior() instanceof ParallelMultiInstanceBehavior) && !(userTask.getBehavior() instanceof SequentialMultiInstanceBehavior)) {//不是多实例需要设置下一节点办理人
                                            pd.put("user_id", user_id);
                                            PageData uPd = userService.findUserInfo(pd);
                                            if (assignPd.get("assign_mode").equals("2")) {//用户组选择
                                                if (Verify.verifyIsNotNull(pd.get("assign_content"))) {
                                                    pd.put("group_id", pd.get("assign_content"));
                                                    List<PageData> groupList = groupService.findGroupUser(pd);
                                                    for (PageData groupPd : groupList) {
                                                        taskService.setAssignee(task.getId(), groupPd.get("user_id").toString());
                                                    }
                                                }
                                            } else if (assignPd.get("assign_mode").equals("4")) {//部门经理
                                                if (Verify.verifyIsNotNull(uPd.get("depart_leader"))) {
                                                    taskService.setAssignee(task.getId(), uPd.get("depart_leader").toString().split("#")[0]);
                                                }
                                            } else if (assignPd.get("assign_mode").equals("5")) {//上级领导
                                                if (Verify.verifyIsNotNull(uPd.get("direct_leader"))) {
                                                    taskService.setAssignee(task.getId(), uPd.get("direct_leader").toString().split("#")[0]);
                                                }
                                            } else if (assignPd.get("assign_mode").equals("6")) {//分管领导
                                                if (Verify.verifyIsNotNull(uPd.get("branch_leader"))) {
                                                    taskService.setAssignee(task.getId(), uPd.get("branch_leader").toString().split("#")[0]);
                                                }
                                            } else if (assignPd.get("assign_mode").equals("7")) {//流程发起人
                                                String uId = processInstance.getStartUserId().split("_")[1];
                                                taskService.setAssignee(task.getId(), uId);
                                            } else if (assignPd.get("assign_mode").equals("8") || assignPd.get("assign_mode").equals("9")) {//指定范围选择 //代理人
                                                if (Verify.verifyIsNotNull(assignPd.get("assign_content"))) {
                                                    String[] assign_content = assignPd.get("assign_content").toString().split("#");
                                                    String[] uids = assign_content[0].split(",");
                                                    for (int j = 0; j < uids.length; j++) {
                                                        taskService.setAssignee(task.getId(), uids[j]);
                                                    }
                                                }
                                            } else if (assignPd.get("assign_mode").equals("10")) {//候选人
                                                if (Verify.verifyIsNotNull(assignPd.get("assign_content"))) {
                                                    String[] assign_content = assignPd.get("assign_content").toString().split("#");
                                                    String[] uids = assign_content[0].split(",");
                                                    for (int j = 0; j < uids.length; j++) {
                                                        taskService.addCandidateUser(task.getId(), uids[j]);
                                                    }
                                                }
                                            } else if (assignPd.get("assign_mode").equals("11")) {//候选组
                                                if (Verify.verifyIsNotNull(assignPd.get("assign_content"))) {
                                                    String[] assign_content = assignPd.get("assign_content").toString().split("#");
                                                    String[] uids = assign_content[0].split(",");
                                                    for (int j = 0; j < uids.length; j++) {
                                                        taskService.addCandidateGroup(task.getId(), uids[j]);
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
            }
        }
    }


    //查询数据详情
    public PageData findInfo(PageData pd){
        return this.baseMapper.findInfo(pd);
    }

    //删除数据
    @Transactional
    public void delData(PageData pd){
        Vform vform = vformService.getById(pd.get("table_id").toString());
        String[] del_ids = pd.get("ids").toString().split(StringPool.COMMA);
        pd.put("ids", Arrays.asList(del_ids));
        pd.put("table_name",vform.getTable_name());
        this.baseMapper.delData(pd);
        //处理子表
        QueryWrapper formWrapper = new QueryWrapper();
        formWrapper.eq("main_id",pd.get("table_id").toString());
        List<Vform> vforms = vformService.list(formWrapper);
        for (Vform form:vforms) {
            pd.put("main_ids", Arrays.asList(del_ids));
            pd.put("table_name",form.getTable_name());
            this.baseMapper.delChildData(pd);
        }
    }

    //更新状态
    public void updateStatus(PageData pd){
        this.baseMapper.updateStatus(pd);
    }



    //流程退回
    @Transactional
    public void rejectAnyNod(PageData pd){
        Vform vform = vformService.getById(pd.get("table_id").toString());
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("table_id",vform.getId());
        List<Vfield> vfields = vfieldService.list(queryWrapper);
        StringBuffer paramField = new StringBuffer();
        String[] hideFieldKey = pd.get("hideFieldKey").toString().split(",");
        for (Vfield vfield : vfields) {
            if(!vfield.getField_type().equals("batch")){
                int index = ArrayUtil.indexOf(hideFieldKey,vfield.getField_name());
                if(index==-1){
                    paramField.append(vfield.getField_name()+"=#{"+vfield.getField_name()+"}").append(",");
                }
            }
            //处理图片
            if(vfield.getField_type().equals("uploadFile")||vfield.getField_type().equals("uploadImg")){
                List<PageData> list = (List<PageData>)pd.get(vfield.getField_name());
                pd.put(vfield.getField_name(), JsonToMap.list2json(list));
            }
        }
        String id = pd.get("id").toString();
        String time = DateTimeUtil.getDateTimeStr();
        String authParams = SecurityContextHolder.getContext().getAuthentication().getName();
        pd.put("update_time",time);
        pd.put("update_user",authParams.split(":")[1]);
        pd.put("paramField", paramField);
        pd.put("table_name", vform.getTable_name());
        this.baseMapper.updateData(pd);
        //处理子表
        QueryWrapper formWrapper = new QueryWrapper();
        formWrapper.eq("main_id",pd.get("table_id").toString());
        List<Vform> vforms = vformService.list(formWrapper);
        for (Vform form:vforms) {
            QueryWrapper fieldWrapper = new QueryWrapper();
            fieldWrapper.eq("table_id",form.getId());
            List<Vfield> fields = vfieldService.list(fieldWrapper);
            StringBuffer childParamField = new StringBuffer();
            StringBuffer field_k = new StringBuffer();
            StringBuffer field_v = new StringBuffer();
            List<String> uploadFiledName = new ArrayList<String>();
            for (Vfield field : fields) {
                int index = ArrayUtil.indexOf(hideFieldKey,field.getField_name());
                if(index==-1){
                    childParamField.append(field.getField_name()+"=#{"+field.getField_name()+"}").append(",");
                    field_k.append(field.getField_name()).append(",");
                    field_v.append("#{"+field.getField_name()+"}").append(",");
                }
                //处理图片
                if(field.getField_type().equals("uploadFile")||field.getField_type().equals("uploadImg")){
                    uploadFiledName.add(field.getField_name());
                }
            }
            List<Map> dataList = (List<Map>)pd.get(form.getTable_name());
            List<String> fieldIds = new ArrayList<String>();
            for (Map map:dataList) {
                PageData p = PageData.getInstance().map2Pd(map);
                uploadFiledName.forEach(field_name->{
                    List<PageData> list = (List<PageData>)p.get(field_name);
                    p.put(field_name, JsonToMap.list2json(list));
                });
                p.put("main_id",id);
                p.put("table_name", form.getTable_name());
                p.put("paramField", childParamField);
                p.put("update_time",time);
                p.put("update_user",authParams.split(":")[1]);
                if(Verify.verifyIsNotNull(p.get("id"))){
                    fieldIds.add(p.get("id").toString());
                    this.baseMapper.updateData(p);
                }else{
                    p.put("param_key", field_k);
                    p.put("param_value", field_v);
                    p.put("id",GuidUtil.getUuid());
                    p.put("create_time",time);
                    p.put("create_user",authParams.split(":")[1]);
                    p.put("create_organize",authParams.split(":")[2]);
                    p.put("menu_id",pd.get("menu_id"));
                    fieldIds.add(p.get("id").toString());
                    this.baseMapper.saveData(p);
                }
            }
            //删除已经删除的子表数据
            pd.put("fieldIds",fieldIds);
            pd.put("table_name",form.getTable_name());
            this.baseMapper.delMyChildData(pd);
        }
        //流程驳回
        String user_id = authParams.split(":")[1];
        String taskId = pd.get("taskId").toString();
        String flowElementId = pd.get("flowElementId").toString();

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
            targetNode = new ProcessServiceImpl().findFirstActivityNode(currentTask.getProcessDefinitionId());
        }else{
            //获取目标节点定义
            targetNode = (FlowNode)process.getFlowElement(flowElementId);
        }
        //如果不是同一个流程(子流程)不能驳回
        if (targetNode == null){
//            throw new ActivitiException("目标节点为空");
            throw new ActivitiException("不是同一个流程(子流程)不能驳回");
        }
        if (!(currFlow.getParentContainer().equals(targetNode.getParentContainer()))) {
            throw new ActivitiException("不是同一个流程(子流程)不能驳回");
        }
        //删除当前运行任务
        String executionEntityId = managementService.executeCommand(new ProcessServiceImpl.DeleteTaskCmd(currentTask.getId()));
        //流程执行到来源节点
        managementService.executeCommand(new ProcessServiceImpl.SetFLowNodeAndGoCmd(targetNode, executionEntityId));
        System.out.println("流程驳回成功<<<<<<<<<<<<<<<<<<<<<<<");
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



    //任务委派
    @Transactional
    public void delegateTask(PageData pd){
        Vform vform = vformService.getById(pd.get("table_id").toString());
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("table_id",vform.getId());
        List<Vfield> vfields = vfieldService.list(queryWrapper);
        StringBuffer paramField = new StringBuffer();
        String[] hideFieldKey = pd.get("hideFieldKey").toString().split(",");
        for (Vfield vfield : vfields) {
            if(!vfield.getField_type().equals("batch")){
                int index = ArrayUtil.indexOf(hideFieldKey,vfield.getField_name());
                if(index==-1){
                    paramField.append(vfield.getField_name()+"=#{"+vfield.getField_name()+"}").append(",");
                }
            }
            //处理图片
            if(vfield.getField_type().equals("uploadFile")||vfield.getField_type().equals("uploadImg")){
                List<PageData> list = (List<PageData>)pd.get(vfield.getField_name());
                pd.put(vfield.getField_name(), JsonToMap.list2json(list));
            }
        }
        String id = pd.get("id").toString();
        String time = DateTimeUtil.getDateTimeStr();
        String authParams = SecurityContextHolder.getContext().getAuthentication().getName();
        pd.put("update_time",time);
        pd.put("update_user",authParams.split(":")[1]);
        pd.put("paramField", paramField);
        pd.put("table_name", vform.getTable_name());
        this.baseMapper.updateData(pd);
        //处理子表
        QueryWrapper formWrapper = new QueryWrapper();
        formWrapper.eq("main_id",pd.get("table_id").toString());
        List<Vform> vforms = vformService.list(formWrapper);
        for (Vform form:vforms) {
            System.out.println(form);
            QueryWrapper fieldWrapper = new QueryWrapper();
            fieldWrapper.eq("table_id",form.getId());
            List<Vfield> fields = vfieldService.list(fieldWrapper);
            StringBuffer childParamField = new StringBuffer();
            StringBuffer field_k = new StringBuffer();
            StringBuffer field_v = new StringBuffer();
            List<String> uploadFiledName = new ArrayList<String>();
            for (Vfield field : fields) {
                int index = ArrayUtil.indexOf(hideFieldKey,field.getField_name());
                if(index==-1){
                    childParamField.append(field.getField_name()+"=#{"+field.getField_name()+"}").append(",");
                    field_k.append(field.getField_name()).append(",");
                    field_v.append("#{"+field.getField_name()+"}").append(",");
                }
                //处理图片
                if(field.getField_type().equals("uploadFile")||field.getField_type().equals("uploadImg")){
                    uploadFiledName.add(field.getField_name());
                }
            }
            List<Map> dataList = (List<Map>)pd.get(form.getTable_name());
            List<String> fieldIds = new ArrayList<String>();
            for (Map map:dataList) {
                PageData p = PageData.getInstance().map2Pd(map);
                uploadFiledName.forEach(field_name->{
                    List<PageData> list = (List<PageData>)p.get(field_name);
                    p.put(field_name, JsonToMap.list2json(list));
                });
                p.put("main_id",id);
                p.put("table_name", form.getTable_name());
                p.put("paramField", childParamField);
                p.put("update_time",time);
                p.put("update_user",authParams.split(":")[1]);
                if(Verify.verifyIsNotNull(p.get("id"))){
                    fieldIds.add(p.get("id").toString());
                    this.baseMapper.updateData(p);
                }else{
                    p.put("param_key", field_k);
                    p.put("param_value", field_v);
                    p.put("id",GuidUtil.getUuid());
                    p.put("create_time",time);
                    p.put("create_user",authParams.split(":")[1]);
                    p.put("create_organize",authParams.split(":")[2]);
                    p.put("menu_id",pd.get("menu_id"));
                    fieldIds.add(p.get("id").toString());
                    this.baseMapper.saveData(p);
                }
            }
            //删除已经删除的子表数据
            pd.put("fieldIds",fieldIds);
            pd.put("table_name",form.getTable_name());
            this.baseMapper.delMyChildData(pd);
        }

        //任务委派
        String taskId = pd.get("taskId").toString();
        String userId = pd.get("userId").toString();
        String userName = pd.get("userName").toString();
        taskService.delegateTask(taskId, userId);
        //添加备注
        Task currentTask = taskService.createTaskQuery().taskId(taskId).singleResult();
        taskService.addComment(taskId,currentTask.getProcessInstanceId(),"任务委托给："+userName);
    }




}