package com.qingfeng.activiti.controller;

import cn.hutool.core.bean.BeanUtil;
import com.qingfeng.activiti.service.ProcessHistoryService;
import com.qingfeng.base.controller.BaseController;
import com.qingfeng.system.service.IUserService;
import com.qingfeng.utils.Json;
import com.qingfeng.utils.Page;
import com.qingfeng.utils.PageData;
import com.qingfeng.utils.Verify;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.*;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ProjectName ProcessHistoryController
 * @author Administrator
 * @version 1.0.0
 * @Description 历史流程信息
 * @createTime 2021/8/22 0022 12:54
 */
@Slf4j
@Validated
@Controller
@RequestMapping(value = "/activiti/processHistory")
public class ProcessHistoryController extends BaseController {

    @Autowired
    private HistoryService historyService;
    @Autowired
    private ProcessHistoryService processHistoryService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private IUserService userService;


    //=====================================历史任务查询======================================

    /** 
     * @Description: findTaskList
     * @Param: [page, request, response, session] 
     * @return: void 
     * @Author: anxingtao
     * @Date: 2020-9-9 17:30 
     */
    @GetMapping("/findTaskListPage")
    @PreAuthorize("hasAnyAuthority('processHistory:task')")
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
        List<HistoricTaskInstance> tasks = new ArrayList<HistoricTaskInstance>();
        HistoricTaskInstanceQuery taskQuery = historyService.createHistoricTaskInstanceQuery();
        if(Verify.verifyIsNotNull(pd.get("name"))){
            taskQuery = taskQuery.taskNameLike(pd.get("name").toString());
        }
        if(Verify.verifyIsNotNull(pd.get("key"))){
            taskQuery = taskQuery.taskDefinitionKeyLike(pd.get("key").toString());
        }
        if(pd.get("type").equals("userTask")){
            //获取当前用户
            if (Verify.verifyIsNotNull(pd.get("processInstanceId"))) {
                taskQuery = taskQuery.processInstanceId(pd.get("processInstanceId").toString())
                        .taskAssignee(user_id);
            } else {
                taskQuery = taskQuery.taskAssignee(user_id);
            }
        }else if(pd.get("type").equals("allTask")){
            if (Verify.verifyIsNotNull(pd.get("processInstanceId"))) {
                taskQuery = taskQuery.processInstanceId(pd.get("processInstanceId").toString());
            }
        }
        if(Verify.verifyIsNull(pd.get("finished"))){
            pd.put("finished","2");
        }
        if (pd.get("finished").equals("0")){// 0：已完成的任务  1：未完成的任务 2：全部任务（默认）
            taskQuery.finished();
        }else if (pd.get("finished").equals("1")){// 0：已完成的任务  1：未完成的任务 2：全部任务（默认）
            taskQuery.unfinished();
        }

        tasks = taskQuery.orderByTaskCreateTime().desc().listPage(firstResult,page.getShowCount());
        //获取总页数
        int total = (int) taskQuery.count();
        List<PageData> allList = new ArrayList<PageData>();
        for (HistoricTaskInstance task : tasks) {
            PageData p = PageData.getInstance().objToPd(task);
            p.put("id",task.getId());
            //查询代办用户及代办用户组名称
            if(Verify.verifyIsNotNull(p.get("assignee"))){
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
            HistoricProcessInstance pi = historyService.createHistoricProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
            if(Verify.verifyIsNotNull(pi)){
                p.put("businessKey",pi.getBusinessKey());
                ProcessDefinition procdef = repositoryService.createProcessDefinitionQuery().processDefinitionId(pi.getProcessDefinitionId()).singleResult();
                p.put("procdef_name",procdef.getName());
                String start_user_id = pi.getStartUserId();
                pd.put("user_id", start_user_id.split("_")[1]);
                PageData uPd = userService.findUserInfo(pd);
                p.put("start_user_name", uPd.get("name"));
                p.put("start_time", pi.getStartTime());
            }else{
                p.put("businessKey","");
                p.put("procdef_name","");
            }
            //处理参数
            p.put("startTime",task.getStartTime());
            p.put("endTime",task.getEndTime());
            p.put("processInstanceId",task.getProcessInstanceId());
            allList.add(p);
        }
        Json json = new Json();
        json.setMsg("获取数据成功。");
        json.setData(allList);
        json.setCount(total);
        json.setSuccess(true);
        this.writeJson(response,json);
    }


    //=====================================历史流程实例查询======================================

    /**
     * @Description: findInstanceListPage
     * @Param: [page, request, response, session]
     * @return: void
     * @Author: anxingtao
     * @Date: 2020-9-10 23:14
     */
    @GetMapping("/findInstanceListPage")
    @PreAuthorize("hasAnyAuthority('processHistory:instance')")
    public void findInstanceListPage(Page page, HttpServletRequest request, HttpServletResponse response, HttpSession session) throws Exception {
        PageData pd = new PageData(request);
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
        //历史流程实例
        List<HistoricProcessInstance> instanceList = new ArrayList<HistoricProcessInstance>();
        HistoricProcessInstanceQuery instanceQuery = historyService.createHistoricProcessInstanceQuery();
        if (Verify.verifyIsNotNull(pd.get("processDefinitionKey"))) {
            instanceQuery = instanceQuery.processDefinitionKey(pd.get("processDefinitionKey").toString());//根据流程定义Key查询
        }
        if (Verify.verifyIsNotNull(pd.get("processDefinitionName"))) {
            instanceQuery = instanceQuery.processDefinitionName(pd.get("processDefinitionName").toString());//根据流程定义name查询
        }
        if (Verify.verifyIsNotNull(pd.get("businessKey"))) {
            instanceQuery = instanceQuery.processInstanceBusinessKey(pd.get("businessKey").toString());//根据业务主键查询
        }
        if(Verify.verifyIsNull(pd.get("finished"))){
            pd.put("finished","2");
        }
        if (pd.get("finished").equals("0")){// 0：已完成的任务  1：未完成的任务 2：全部任务（默认）
            instanceQuery.finished();
        }else if (pd.get("finished").equals("1")){// 0：已完成的任务  1：未完成的任务 2：全部任务（默认）
            instanceQuery.unfinished();
        }
        instanceList = instanceQuery.orderByProcessInstanceStartTime().desc().listPage(firstResult,page.getShowCount());
        //获取总页数
        int total = (int) instanceQuery.count();

        List<Map<String ,Object>> allList = new ArrayList<>();
        for (HistoricProcessInstance processInstance : instanceList) {
            Map p = BeanUtil.beanToMap(processInstance);
            p.put("id",processInstance.getId());
            if(Verify.verifyIsNotNull(p.get("startUserId"))){
                pd.put("user_id",p.get("startUserId").toString().split("_")[1]);
                PageData uPd = userService.findUserInfo(pd);
                p.put("startUserName",uPd.get("name"));
            }else{
                p.put("startUserName","");
            }
            allList.add(p);
        }
        System.out.println(allList);
        Json json = new Json();
        json.setMsg("获取数据成功。");
        json.setData(allList);
        json.setCount(total);
        json.setSuccess(true);
        this.writeJson(response,json);
    }

    //=====================================activityIndex历史行为====================================

    /**
     * @Description: findActivityListPage
     * @Param: [page, request, response, session]
     * @return: void
     * @Author: anxingtao
     * @Date: 2020-9-10 23:37
     */
    @GetMapping("/findActivityListPage")
    @PreAuthorize("hasAnyAuthority('processHistory:activity')")
    public void findActivityListPage(Page page, HttpServletRequest request, HttpServletResponse response, HttpSession session) throws Exception {
        PageData pd = new PageData(request);
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
        //历史行为
        List<HistoricActivityInstance> activityList = new ArrayList<HistoricActivityInstance>();
        HistoricActivityInstanceQuery activityQuery = historyService.createHistoricActivityInstanceQuery();
        if (Verify.verifyIsNotNull(pd.get("processInstanceId"))) {
            activityQuery = activityQuery.processInstanceId(pd.get("processInstanceId").toString());//根据流程实例查询
        }
        activityQuery = activityQuery.orderByHistoricActivityInstanceStartTime().desc();
        activityList = activityQuery.listPage(firstResult,page.getShowCount());
        //获取总页数
        int total = (int) activityQuery.count();

        List<Map<String ,Object>> allList = new ArrayList<>();
        for (HistoricActivityInstance activity : activityList) {
            Map p = BeanUtil.beanToMap(activity);
            p.put("id",activity.getId());
            //查询办理人
            p.put("assigneeName", "");
            if(Verify.verifyIsNotNull(activity.getAssignee())){
                pd.put("user_id", activity.getAssignee());
                PageData userPd = userService.findUserInfo(pd);
                if (Verify.verifyIsNotNull(userPd)) {
                    p.put("assigneeName", userPd.get("name"));
                }
            }
            //查询审批意见
            if(Verify.verifyIsNotNull(activity.getTaskId())){
                List<Comment> comments = taskService.getTaskComments(activity.getTaskId());
                if(comments.size()>0){
                    p.put("comment_msg",comments.get(0).getFullMessage());
                    p.put("comment_time",comments.get(0).getTime());
                }
            }
            allList.add(p);
        }
        System.out.println(allList);
        Json json = new Json();
        json.setMsg("获取数据成功。");
        json.setData(allList);
        json.setCount(total);
        json.setSuccess(true);
        this.writeJson(response,json);
    }

    public List<PageData> getHistoryTaskByTaskId(String taskId){
        HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
        String processInstanceId = historicTaskInstance.getProcessInstanceId();
        List<HistoricTaskInstance> historicTaskInstanceList = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId).orderByTaskCreateTime().asc().list();

        List<PageData> approvalEntityList = new ArrayList<PageData>();
        String approvalSuggestion = "";
        Object approvalStatus = "";
        HistoricVariableInstance historicVariableInstance = null;
        List<Comment> commentList = null;
        for(HistoricTaskInstance item : historicTaskInstanceList){
            PageData approvalEntity = new PageData();
            commentList = taskService.getTaskComments(item.getId());
            if(commentList != null && !commentList.isEmpty()) {
                approvalSuggestion = commentList.get(0).getFullMessage();
            }else{
                approvalSuggestion = "";
            }
            historicVariableInstance = historyService.createHistoricVariableInstanceQuery()
                    .taskId(item.getId()).variableName("approvalStatus").singleResult();
            if(historicVariableInstance != null) {
                approvalStatus = historicVariableInstance.getValue();
                approvalEntity.put("approvalStatue", StringUtils.isBlank(approvalStatus.toString()) ? 0 : Integer.parseInt(approvalStatus.toString()));
            }
            approvalEntity.put("operateDate",item.getEndTime());
            approvalEntity.put("operato",item.getAssignee());
            approvalEntity.put("taskName",item.getName());
            approvalEntity.put("approvalSuggestion",approvalSuggestion);
            approvalEntityList.add(approvalEntity);
            System.out.println("approvalEntity:"+approvalEntity);
        }

        return approvalEntityList;
    }


    //====================================detailIndex历史流程明细==========================

    /**
     * @Description: findDetailListPage
     * @Param: [page, request, response, session]
     * @return: void
     * @Author: anxingtao
     * @Date: 2020-9-10 23:53
     */
    @GetMapping("/findDetailListPage")
    @PreAuthorize("hasAnyAuthority('processHistory:detail')")
    public void findDetailListPage(Page page, HttpServletRequest request, HttpServletResponse response, HttpSession session) throws Exception {
        PageData pd = new PageData(request);
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
        //历史流程明细
        List<HistoricDetail> detailList = new ArrayList<HistoricDetail>();
        HistoricDetailQuery detailQuery = historyService.createHistoricDetailQuery();
        if (Verify.verifyIsNotNull(pd.get("processInstanceId"))) {
            detailQuery = detailQuery.processInstanceId(pd.get("processInstanceId").toString());//根据流程实例查询
        }
        detailList = detailQuery.listPage(firstResult,page.getShowCount());
        //获取总页数
        int total = (int) detailQuery.count();

        List<Map<String ,Object>> allList = new ArrayList<>();
        for (HistoricDetail detail : detailList) {
            Map p = BeanUtil.beanToMap(detail);
            p.put("id",detail.getId());
            allList.add(p);
        }
        System.out.println(allList);
        Json json = new Json();
        json.setMsg("获取数据成功。");
        json.setData(allList);
        json.setCount(total);
        json.setSuccess(true);
        this.writeJson(response,json);
    }



    //=============================我发起的流程==============================

    /**
     * @Description: findMyInstanceListPage
     * @Param: [page, request, response, session]
     * @return: void
     * @Author: anxingtao
     * @Date: 2020-9-11 0:24
     */
    @GetMapping("/findMyInstanceListPage")
    @PreAuthorize("hasAnyAuthority('processHistory:myInstance')")
    public void findMyInstanceListPage(Page page, HttpServletRequest request, HttpServletResponse response, HttpSession session) throws Exception {
        PageData pd = new PageData(request);
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
        //历史流程实例
        List<HistoricProcessInstance> instanceList = new ArrayList<HistoricProcessInstance>();
        HistoricProcessInstanceQuery instanceQuery = historyService.createHistoricProcessInstanceQuery();
        if (Verify.verifyIsNotNull(pd.get("processDefinitionKey"))) {
            instanceQuery = instanceQuery.processDefinitionKey(pd.get("processDefinitionKey").toString());//根据流程定义Key查询
        }
        if (Verify.verifyIsNotNull(pd.get("processDefinitionName"))) {
            instanceQuery = instanceQuery.processDefinitionName(pd.get("processDefinitionName").toString());//根据流程定义name查询
        }
        if (Verify.verifyIsNotNull(pd.get("businessKey"))) {
            instanceQuery = instanceQuery.processInstanceBusinessKey(pd.get("businessKey").toString());//根据业务主键查询
        }
        //获取当前用户
        String userParams = SecurityContextHolder.getContext().getAuthentication().getName();
        String user_id = userParams.split(":")[1];
        String organize_id = userParams.split(":")[2];
        instanceQuery.startedBy(organize_id+"_"+user_id);
        if(Verify.verifyIsNull(pd.get("finished"))){
            pd.put("finished","2");
        }
        if (pd.get("finished").equals("0")){// 0：已完成的任务  1：未完成的任务 2：全部任务（默认）
            instanceQuery.finished();
        }else if (pd.get("finished").equals("1")){// 0：已完成的任务  1：未完成的任务 2：全部任务（默认）
            instanceQuery.unfinished();
        }
        instanceList = instanceQuery.orderByProcessInstanceStartTime().desc().listPage(firstResult,page.getShowCount());
        //获取总页数
        int total = (int) instanceQuery.count();
        List<Map<String ,Object>> allList = new ArrayList<>();
        for (HistoricProcessInstance processInstance : instanceList) {
            Map p = BeanUtil.beanToMap(processInstance);
            p.put("id",processInstance.getId());
            ProcessDefinition procdef = repositoryService.createProcessDefinitionQuery().processDefinitionId(processInstance.getProcessDefinitionId()).singleResult();
            p.put("procdef_name",procdef.getName());
            pd.put("user_id",processInstance.getStartUserId().split("_")[1]);
            PageData uPd = userService.findUserInfo(pd);
            p.put("assignee_name",uPd.get("name"));
            //查询当前任务
            if(Verify.verifyIsNull(processInstance.getEndTime())){//未结束-查询当前办理任务
                List<Task> taskList = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
                StringBuffer sb = new StringBuffer();
                for (Task task:taskList) {
                    String assignee_name = "";
                    if(Verify.verifyIsNotNull(task.getAssignee())){
                        pd.put("user_id",task.getAssignee());
                        PageData userPd = userService.findUserInfo(pd);
                        if(Verify.verifyIsNotNull(userPd)){
                            assignee_name = userPd.get("name").toString();
                        }
                    }
                    if(Verify.verifyIsNotNull(assignee_name)){
                        sb.append(task.getName()).append("【").append(assignee_name).append("】").append(",");
                    }else{
                        sb.append(task.getName()).append("【未指定】").append(",");
                    }
                }
                if(sb.length()>0){
                    p.put("taskName",sb.substring(0,sb.length()-1));
                }
            }
            //业务表单
            String businessKey = processInstance.getBusinessKey();
            allList.add(p);
        }
        System.out.println(allList);
        Json json = new Json();
        json.setMsg("获取数据成功。");
        json.setData(allList);
        json.setCount(total);
        json.setSuccess(true);
        this.writeJson(response,json);
    }


    //========================删除历史流程实例==============================
    /**
     * @Description: delInstance  删除历史流程实例
     * @Param: [request, response, session]
     * @return: void
     * @Author: anxingtao
     * @Date: 2020-9-11 0:34
     */
    @DeleteMapping("/delInstance/{ids}")
    @PreAuthorize("hasAnyAuthority('processHistory:delInstance')")
    public void delInstance(@NotBlank(message = "{required}") @PathVariable String ids,HttpServletRequest request, HttpServletResponse response, HttpSession session) throws Exception {
        Json json = new Json();
        try{
            String[] del_ids = ids.split(",");
            for (String id: del_ids) {
                historyService.deleteHistoricProcessInstance(id);
            }
            json.setMsg("操作成功。");
            json.setSuccess(true);
        }catch (Exception e){
            json.setMsg("操作失败："+e.getMessage());
            json.setSuccess(false);
        }
        this.writeJson(response,json);
    }

    /**
     * @Description: delTask 删除历史流程任务
     * @Param: [request, response, session]
     * @return: void
     * @Author: anxingtao
     * @Date: 2020-9-11 0:34
     */
    @DeleteMapping("/delTask/{ids}")
    @PreAuthorize("hasAnyAuthority('processHistory:delTask')")
    public void delTask(@NotBlank(message = "{required}") @PathVariable String ids,HttpServletRequest request, HttpServletResponse response,HttpSession session) throws Exception {
        String[] del_ids = ids.split(",");
        for (String id: del_ids) {
            historyService.deleteHistoricTaskInstance(id);
        }
        Json json = new Json();
        json.setMsg("操作成功。");
        json.setSuccess(true);
        this.writeJson(response,json);
    }


    //======================================查询案例====================================================
    /**
     * @Description: getHistoryTaskList 获取流程的历史任务
     * @Param: [map, request, response]
     * @return: void
     * @Author: anxingtao
     * @Date: 2020-8-18 23:06
     */
    @GetMapping("/getHistoryTaskList")
    @PreAuthorize("hasAnyAuthority('processHistory:task')")
    public void getHistoryTaskList(HttpServletRequest request, HttpServletResponse response) throws Exception {
        PageData pd = new PageData(request);
        //finished 0：已完成的任务  1：未完成的任务 2：全部任务（默认）
        String finished = "2";
        if(Verify.verifyIsNotNull(pd.get("finished"))){
            finished = pd.get("finished").toString();
        }
        List<HistoricTaskInstance> list = processHistoryService.getHistoryTaskList(finished, pd.get("processInstanceId").toString());
        Json json = new Json();
        json.setMsg("流程启动成功。");
        json.setData(list);
        json.setSuccess(true);
        this.writeJson(response,json);
    }


    /**
     * @Description: getHistoryActInstanceList 获取流程的历史活动
     * @Param: [map, request, response]
     * @return: void
     * @Author: anxingtao
     * @Date: 2020-8-18 23:15
     */
    @GetMapping("/getHistoryActInstanceList")
    @PreAuthorize("hasAnyAuthority('processHistory:instance')")
    public void getHistoryActInstanceList(HttpServletRequest request, HttpServletResponse response) throws Exception {
        PageData pd = new PageData(request);
        //finished 0：已完成的任务  1：未完成的任务 2：全部任务（默认）
        String finished = "2";
        if(Verify.verifyIsNotNull(pd.get("finished"))){
            finished = pd.get("finished").toString();
        }
        List<HistoricActivityInstance> list = processHistoryService.getHistoryActInstanceList(finished, pd.get("processInstanceId").toString());
        Json json = new Json();
        json.setMsg("流程启动成功。");
        json.setData(list);
        json.setSuccess(true);
        this.writeJson(response,json);
    }


    /**
     * @Description: getHistoryProcessVariables 获取流程历史流程变量
     * @Param: [map, request, response]
     * @return: void
     * @Author: anxingtao
     * @Date: 2020-8-18 23:19
     */
    @GetMapping("/getHistoryProcessVariables")
    @PreAuthorize("hasAnyAuthority('processHistory:variables')")
    public void getHistoryProcessVariables(ModelMap map, HttpServletRequest request, HttpServletResponse response) throws Exception {
        PageData pd = new PageData(request);
        List<HistoricVariableInstance> list = processHistoryService.getHistoryProcessVariables(pd.get("processInstanceId").toString());
        Json json = new Json();
        json.setMsg("流程启动成功。");
        json.setData(list);
        json.setSuccess(true);
        this.writeJson(response,json);
    }


    /** 
     * @Description: getFinishedInstanceList 获取已归档的流程实例
     * @Param: [map, request, response] 
     * @return: void 
     * @Author: anxingtao
     * @Date: 2020-8-18 23:21 
     */
    @GetMapping("/getFinishedInstanceList")
    @PreAuthorize("hasAnyAuthority('processHistory:instance')")
    public void getFinishedInstanceList(Page page, ModelMap map, HttpServletRequest request, HttpServletResponse response) throws Exception {
        PageData pd = new PageData(request);
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
        Page list = processHistoryService.getFinishedInstanceList(page.getIndex(),  page.getShowCount(),pd.get("processDefinitionId").toString()); //流程定义ID
        Json json = new Json();
        json.setMsg("流程启动成功。");
        json.setData(list);
        json.setSuccess(true);
        this.writeJson(response,json);
    }



    /**
     * @Description: queryHistoricInstance  获取历史流程实例（所有已发起的流程）
     * @Param: [page, map, request, response]
     * @return: void
     * @Author: anxingtao
     * @Date: 2020-8-18 23:27
     */
    @GetMapping("/queryHistoricInstance")
    @PreAuthorize("hasAnyAuthority('processHistory:instance')")
    public void queryHistoricInstance(Page page, ModelMap map, HttpServletRequest request, HttpServletResponse response) throws Exception {
        PageData pd = new PageData(request);
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
        Page list = processHistoryService.queryHistoricInstance( page.getIndex(),page.getShowCount(),pd.get("processDefinitionId").toString());//流程定义ID
        Json json = new Json();
        json.setMsg("流程启动成功。");
        json.setData(list);
        json.setSuccess(true);
        this.writeJson(response,json);
    }
    



}
