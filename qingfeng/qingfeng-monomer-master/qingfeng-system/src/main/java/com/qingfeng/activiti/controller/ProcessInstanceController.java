package com.qingfeng.activiti.controller;

import cn.hutool.core.bean.BeanUtil;
import com.qingfeng.base.controller.BaseController;
import com.qingfeng.system.service.IUserService;
import com.qingfeng.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ExecutionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ProjectName ProcessInstanceController
 * @author Administrator
 * @version 1.0.0
 * @Description 流程实例
 * @createTime 2021/8/17 0017 21:20
 */
@Slf4j
@Validated
@Controller
@RequestMapping(value = "/activiti/processInstance")
public class ProcessInstanceController extends BaseController {

    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private IUserService userService;


    /**
     * @title findListPage
     * @description 查询流程定义列表
     * @author Administrator
     * @updateTime 2021/8/17 0017 21:21
     */
    @GetMapping("/findListPage")
    @PreAuthorize("hasAnyAuthority('processInstance:info')")
    public void findListPage(Page page, HttpServletRequest request, HttpServletResponse response) throws IOException {
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

        ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery();//创建一个流程实例查询
        List<ProcessInstance> processInstances = new ArrayList<ProcessInstance>();
        if (Verify.verifyIsNotNull(pd.get("processInstanceId"))) {
            processInstanceQuery = processInstanceQuery.processInstanceId(pd.get("processInstanceId").toString());//根据流程实例查询
        }
        if (Verify.verifyIsNotNull(pd.get("processDefinitionName"))) {
            processInstanceQuery = processInstanceQuery.processDefinitionName(pd.get("processDefinitionName").toString());//根据流程定义name查询
        }
        if (Verify.verifyIsNotNull(pd.get("processDefinitionId"))) {
            processInstanceQuery = processInstanceQuery.processDefinitionId(pd.get("processDefinitionId").toString());//根据流程定义ID查询
        }
        if (Verify.verifyIsNotNull(pd.get("processDefinitionKey"))) {
            processInstanceQuery = processInstanceQuery.processDefinitionKey(pd.get("processDefinitionKey").toString());//根据流程定义Key查询
        }

        processInstanceQuery = processInstanceQuery.orderByProcessInstanceId().desc();

/*
        int firstResult = (pageNum-1) * pageSize;
        int maxResults = pageSize;
*/
        int firstResult = (page.getIndex()-1)*page.getShowCount();
        processInstances = processInstanceQuery.listPage(firstResult,page.getShowCount());
        //获取总页数
        int total = (int) processInstanceQuery.count();
        List<Map<String ,Object>> allList = new ArrayList<>();
        for (ProcessInstance processInstance : processInstances) {
//            Map<String ,Object> map = new HashMap<>();
//            System.out.println("#######:"+processDefinition.getId());
//            String deploymentId = processDefinition.getDeploymentId();
//            Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(deploymentId).singleResult();
//            map.put("processDefinition", PageData.getInstance().objToPd(processDefinition));
//            map.put("deployment",PageData.getInstance().objToPd(deployment));

            Map p = BeanUtil.beanToMap(processInstance);
            if(Verify.verifyIsNotNull(processInstance.getStartUserId())) {
                //根据用户id查询
                pd.put("user_id", processInstance.getStartUserId().split("_")[1]);
                PageData userPd = userService.findUserInfo(pd);
                if (Verify.verifyIsNotNull(userPd)) {
                    p.put("startUserName", userPd.get("name"));
                } else {
                    p.put("startUserName", "");
                }
            }
            p.put("id",processInstance.getId());
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



    /**
     * @title suspensionState
     * @description 中止或激活流程
     * @author Administrator
     * @updateTime 2021/8/17 0017 21:21
     */
    @PostMapping("/suspensionState")
    @PreAuthorize("hasAnyAuthority('processInstance:suspensionState')")
    public void suspensionState(@RequestBody PageData pd, HttpServletResponse response) throws Exception {
        if(pd.get("suspensionState").equals("1")){//中止流程
            runtimeService.suspendProcessInstanceById(pd.get("processInstanceId").toString());
        }else if(pd.get("suspensionState").equals("2")){//激活流程
            runtimeService.activateProcessInstanceById(pd.get("processInstanceId").toString());
        }
        Json json = new Json();
        json.setMsg("操作成功。");
        json.setSuccess(true);
        this.writeJson(response,json);
    }


    /**
     * @title delInstance
     * @description 删除流程实例
     * @author Administrator
     * @updateTime 2021/8/17 0017 21:21
     */
    @DeleteMapping("/{instanceIds}")
    @PreAuthorize("hasAnyAuthority('processInstance:del')")
    public void delInstance(@NotBlank(message = "{required}") @PathVariable String instanceIds,HttpServletRequest request, HttpServletResponse response, HttpSession session) throws Exception {
        PageData pd = new PageData(request);
        String[] ids = instanceIds.split(",");
        for (String id: ids) {
            runtimeService.deleteProcessInstance(id, DateTimeUtil.getDateTimeStr()+"删除流程实例");
        }
        Json json = new Json();
        json.setMsg("操作成功。");
        json.setSuccess(true);
        this.writeJson(response,json);
    }




    /**
     * @title findExecutionListPage
     * @description 查询执行流列表
     * @author Administrator
     * @updateTime 2021/8/17 0017 21:22
     */
    @GetMapping("/findExecutionListPage")
    @PreAuthorize("hasAnyAuthority('processInstance:execution')")
    public void findExecutionListPage(Page page, HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException {
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

        ExecutionQuery executionQuery = runtimeService.createExecutionQuery();//创建一个流程实例查询
        List<Execution> executionList = new ArrayList<Execution>();
        if (Verify.verifyIsNotNull(pd.get("processInstanceId"))) {
            executionQuery = executionQuery.processInstanceId(pd.get("processInstanceId").toString());//根据流程实例查询
        }
        executionQuery = executionQuery.orderByProcessInstanceId().desc();

/*
        int firstResult = (pageNum-1) * pageSize;
        int maxResults = pageSize;
*/
        int firstResult = (page.getIndex()-1)*page.getShowCount();
        executionList = executionQuery.listPage(firstResult,page.getShowCount());
        //获取总页数
        int total = (int) executionQuery.count();
        List<Map<String ,Object>> allList = new ArrayList<>();
        for (Execution execution : executionList) {
            Map p = BeanUtil.beanToMap(execution);
            p.put("id",execution.getId());
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



}
