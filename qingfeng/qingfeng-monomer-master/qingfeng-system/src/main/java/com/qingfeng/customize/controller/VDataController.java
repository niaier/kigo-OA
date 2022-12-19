package com.qingfeng.customize.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.qingfeng.base.controller.BaseController;
import com.qingfeng.customize.service.*;
import com.qingfeng.entity.MyResponse;
import com.qingfeng.entity.QueryRequest;
import com.qingfeng.entity.customize.Vfield;
import com.qingfeng.entity.customize.Vform;
import com.qingfeng.entity.customize.Vmenu;
import com.qingfeng.entity.customize.VmenuTfield;
import com.qingfeng.entity.system.Dictionary;
import com.qingfeng.entity.system.UserOrganize;
import com.qingfeng.exception.MyException;
import com.qingfeng.system.service.IDictionaryService;
import com.qingfeng.system.service.IUserOrganizeService;
import com.qingfeng.system.service.IUserService;
import com.qingfeng.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.model.BpmnModel;
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
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.*;


/**
 * @ProjectName VDataController
 * @author Administrator
 * @version 1.0.0
 * @Description VDataController
 * @createTime 2021/10/1 0001 22:28
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/customize/vdata")
public class VDataController extends BaseController {

    @Autowired
    private IVDataService dataService;
    @Autowired
    private IVmenuService vmenuService;
    @Autowired
    private IVformService vformService;
    @Autowired
    private IVfieldService vfieldService;
    @Autowired
    private IUserOrganizeService userOrganizeService;
    @Autowired
    public IVmenuTfieldService vmenuTfieldService;
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
    private IDictionaryService dictionaryService;

    /**
     * @title findVMenuInfo
     * @description 查询模块菜单信息
     * @author Administrator
     * @updateTime 2021/10/2 0002 22:11
     */
    @GetMapping("/findVMenuInfo")
    public void findVMenuInfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
        PageData pd = new PageData(request);
        Vmenu vmenu = vmenuService.getById(pd.get("menu_id").toString());
        Vform vform = vformService.getById(vmenu.getTable_id());
        vmenu.setVform(vform);
        pd.put("type","0");
        pd.put("table_id",vmenu.getTable_id());
        List<PageData> vmenuTfields = vmenuTfieldService.findFieldList(pd);
        vmenu.setFieldPdList(vmenuTfields);
        this.writeJson(response,vmenu);
    }

    /**
     * @title findVFormInfo
     * @description 查询自定义表单信息
     * @author Administrator
     * @updateTime 2021/10/3 0003 10:21
     */
    @GetMapping("/findVFormInfo")
    public void findVFormInfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
        PageData pd = new PageData(request);
        Vform vform = null;
        if(Verify.verifyIsNotNull(pd.get("formKey"))){
            vform = vformService.getById(pd.get("formKey").toString());
        }else{
            Vmenu vmenu = vmenuService.getById(pd.get("menu_id").toString());
            vform = vformService.getById(vmenu.getTable_id());
        }
        //查询主表信息和主表字段信息-组织动态参数
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("table_id",vform.getId());
        List<Vfield> vfields = vfieldService.list(queryWrapper);
        PageData dynamicData = new PageData();
        for (Vfield vfield:vfields) {
            if("select,checkbox,radio".indexOf(vfield.getField_type())!=-1&&vfield.getDynamic().equals("true")){
                Dictionary dictionary = new Dictionary();
                dictionary.setParent_code(vfield.getDynamicKey());
                List<Dictionary> list = dictionaryService.findList(dictionary);
                List<PageData> dynamicList = new ArrayList<PageData>();
                list.forEach(item->{
                    PageData p = new PageData();
                    p.put("label",item.getName());
                    p.put("value",item.getId());
                    dynamicList.add(p);
                });
                dynamicData.put(vfield.getDynamicKey(),dynamicList);
            }
        }
        vform.setVfields(vfields);
        //查询组织关联子表的动态参数
        QueryWrapper formWrapper = new QueryWrapper();
        formWrapper.eq("main_id",vform.getId());
        List<Vform> listChild = vformService.list(formWrapper);
        for (Vform vformChild:listChild) {
            QueryWrapper qWrapper = new QueryWrapper();
            qWrapper.eq("table_id",vformChild.getId());
            List<Vfield> vfieldsChild = vfieldService.list(qWrapper);

            for (Vfield vfield:vfieldsChild) {
                if("select,checkbox,radio".indexOf(vfield.getField_type())!=-1&&vfield.getDynamic().equals("true")){
                    Dictionary dictionary = new Dictionary();
                    dictionary.setParent_code(vfield.getDynamicKey());
                    List<Dictionary> list = dictionaryService.findList(dictionary);
                    List<PageData> dynamicList = new ArrayList<PageData>();
                    list.forEach(item->{
                        PageData p = new PageData();
                        p.put("label",item.getName());
                        p.put("value",item.getId());
                        dynamicList.add(p);
                    });
                    dynamicData.put(vfield.getDynamicKey(),dynamicList);
                }
            }
            vform.setVfields(vfieldsChild);
        }
        Json json = new Json();
        json.setData(vform);
        json.setObject(dynamicData);
        json.setSuccess(true);
        this.writeJson(response,json);
    }



    /**
     * @title findLinkFormList
     * @description 查询关联表信息
     * @author Administrator
     * @updateTime 2021/10/3 0003 12:31
     */
    @GetMapping("/findLinkFormList")
    public void findLinkFormList(HttpServletRequest request, HttpServletResponse response) throws Exception {
        PageData pd = new PageData(request);
        QueryWrapper formWrapper = new QueryWrapper();
        formWrapper.eq("main_id",pd.get("table_id").toString());
        List<Vform> list = vformService.list(formWrapper);
        for (Vform vform:list) {
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("table_id",vform.getId());
            List<Vfield> vfields = vfieldService.list(queryWrapper);
            vform.setVfields(vfields);
        }
        this.writeJson(response,list);
    }


    /**
     * @title findListPage
     * @description 查询数据列表
     * @author Administrator
     * @updateTime 2021/10/1 0001 22:28
     */
    @GetMapping("/findListPage")
//    @PreAuthorize("hasAnyAuthority('vdata:info')")
    public MyResponse findListPage(QueryRequest queryRequest,HttpServletRequest request) throws Exception {
        PageData pd = new PageData(request);
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
        pd.put("auth_user",user_id);
        pd.put("auth_organize_ids",auth_organize_ids);
        Vmenu vmenu = vmenuService.getById(pd.get("menu_id").toString());
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("menu_id",vmenu.getId());
        pd.put("type","0");
        pd.put("table_id",vmenu.getTable_id());
        List<PageData> vmenuTfields = vmenuTfieldService.findFieldList(pd);
        StringBuffer field = new StringBuffer();
        StringBuffer query_field = new StringBuffer();
        for (PageData mPd : vmenuTfields) {
            field.append("a." + mPd.get("field_name")).append(",");
            if (mPd.get("field_query").equals("true") && pd.containsKey(mPd.get("field_name"))) {//查询字段
                if(Verify.verifyIsNotNull(pd.get(mPd.get("field_name")))){
                    if (mPd.get("query_type").equals("like")) {
                        query_field.append(" and a." + mPd.get("field_name") + " like '%" + pd.get(mPd.get("field_name")) + "%'");
                    } else if (mPd.get("query_type").equals("=") || mPd.get("query_type").equals(">") || mPd.get("query_type").equals(">=") || mPd.get("query_type").equals("!=")) {
                        query_field.append(" and a." + mPd.get("field_name") + " " + mPd.get("query_type") + " '" + pd.get(mPd.get("field_name")) + "'");
                    } else if (mPd.get("query_type").equals("<") || mPd.get("query_type").equals("<=")) {
                        query_field.append(" and a." + mPd.get("field_name") + " " + mPd.get("query_type") + " '" + pd.get(mPd.get("field_name")) + "'");
                    } else if (mPd.get("query_type").equals("is null") || mPd.get("query_type").equals("is not null")) {
                        query_field.append(" and a." + mPd.get("field_name") + " " + mPd.get("query_type"));
                    }
                }
            }
        }
        //是否开启创建时间段查询
        if(vmenu.getOpen_timequery().equals("0")){
            if(Verify.verifyIsNotNull(pd.get("start_time"))){
                query_field.append(" and a.create_time >='" + pd.get("start_time")+"'");
            }
            if(Verify.verifyIsNotNull(pd.get("end_time"))){
                query_field.append(" and a.create_time <='" + pd.get("end_time")+"'");
            }
        }
        //处理排序
        if(Verify.verifyIsNotNull(vmenu.getMain_order())){
            pd.put("order_by",vmenu.getMain_order());
        }else{
            pd.put("order_by"," order by a.create_time desc ");
        }
        //处理约束条件
        if(Verify.verifyIsNotNull(vmenu.getQuery_cond())){
            query_field.append(" "+vmenu.getQuery_cond()+" ");
        }
        Vform vform = vformService.getById(vmenu.getTable_id());
        pd.put("table_name", vform.getTable_name());
        pd.put("field", field.toString());
        pd.put("query_field", query_field.toString());
        IPage<PageData> list = dataService.findListPage(pd, queryRequest);
        for (PageData p:list.getRecords()) {
            QueryWrapper cformWrapper = new QueryWrapper();
            cformWrapper.eq("main_id",vmenu.getTable_id());

            List<Vform> vforms = vformService.list(cformWrapper);
            for (Vform form:vforms) {
                QueryWrapper cfieldWrapper = new QueryWrapper();
                cfieldWrapper.eq("table_id",form.getId());
                List<Vfield> vfs = vfieldService.list(cfieldWrapper);
                StringBuffer cQueryField = new StringBuffer();
                for (Vfield vfield : vfs) {
                    cQueryField.append("a." + vfield.getField_name()).append(",");
                }
                pd.put("field", cQueryField.toString());
                pd.put("table_name",form.getTable_name());
                pd.put("main_id",p.get("id"));
                List<PageData> childList = dataService.findList(pd);
                p.put(form.getTable_name(),childList);
            }
            //处理流程信息
            if(vmenu.getOpen_process().equals("0")){
                String businessKey = "business:customize:"+vmenu.getId()+":"+p.get("id");
                p.put("businessKey",businessKey);
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
                        p.put("assignee",blr.substring(0,blr.length()-1));
                    }
                    if(blzt.length()>0){
                        p.put("dealStatus",blzt.substring(0,blzt.length()-1));
                    }
                    p.put("processStatus","1");
                    p.put("dealTime",DateTimeUtil.getDateTimeStr(taskList.get(0).getCreateTime()));
                }else{
                    List<HistoricProcessInstance> piList = historyService.createHistoricProcessInstanceQuery().processInstanceBusinessKey(businessKey).list();
                    if(piList.size()>0){
                        p.put("dealStatus","流程已结束");
                        p.put("processStatus","2");
                    }else{
                        p.put("dealStatus","流程未启动");
                        p.put("processStatus","0");
                    }
                }
                if(!p.get("processStatus").equals("0")){
                    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(businessKey).singleResult();
                    if(Verify.verifyIsNotNull(processInstance)){
                        p.put("processInstanceId",processInstance.getId());
                    }
                }
            }
        }
        Map<String, Object> dataTable = MyUtil.getDataTable(list);
        MyResponse myResponse = new MyResponse();
        myResponse.data(dataTable);
        return myResponse;
    }


    /**
     * @title save
     * @description 保存方法
     * @author Administrator
     * @updateTime 2021/10/1 0001 22:28
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('vdata:add')")
    public void save(@Valid @RequestBody PageData pd,HttpServletResponse response) throws Exception {
        Json json = new Json();
        try {
            this.dataService.saveVData(pd);
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
     * @title findVDataInfo
     * @description 查询数据表信息
     * @author Administrator
     * @updateTime 2021/10/3 0003 16:22
     */
    @GetMapping("/findVDataInfo")
    public void findVDataInfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
        PageData pd = new PageData(request);
        //查询主表信息
        Vform vform = new Vform();
        if(Verify.verifyIsNotNull(pd.get("table_id"))){
            vform = vformService.getById(pd.get("table_id").toString());
        }else{
            Vmenu vmenu = vmenuService.getById(pd.get("menu_id").toString());
            vform = vformService.getById(vmenu.getTable_id());
        }
        QueryWrapper mfieldWrapper = new QueryWrapper();
        mfieldWrapper.eq("table_id",vform.getId());
        List<Vfield> vfields = vfieldService.list(mfieldWrapper);
        StringBuffer queryField = new StringBuffer();
        for (Vfield vfield : vfields) {
            queryField.append("a." + vfield.getField_name()).append(",");
        }
        pd.put("table_name",vform.getTable_name());
        pd.put("field", queryField.toString());
        PageData p = dataService.findInfo(pd);

        QueryWrapper cformWrapper = new QueryWrapper();
        cformWrapper.eq("main_id",vform.getId());
        List<Vform> vforms = vformService.list(cformWrapper);
        for (Vform form:vforms) {
            QueryWrapper cfieldWrapper = new QueryWrapper();
            cfieldWrapper.eq("table_id",form.getId());
            List<Vfield> vfs = vfieldService.list(cfieldWrapper);
            StringBuffer cQueryField = new StringBuffer();
            for (Vfield vfield : vfs) {
                cQueryField.append("a." + vfield.getField_name()).append(",");
            }
            pd.put("field", cQueryField.toString());
            pd.put("table_name",form.getTable_name());
            pd.put("main_id",pd.get("id"));
            List<PageData> list = dataService.findList(pd);
            p.put(form.getTable_name(),list);
        }
        this.writeJson(response,p);
    }



    /**
     * @title update
     * @description 更新方法
     * @author Administrator
     * @updateTime 2021/10/1 0001 22:28
     */
    @PutMapping
    @PreAuthorize("hasAnyAuthority('vdata:edit')")
    public void update(@Valid @RequestBody PageData pd,HttpServletResponse response) throws Exception {
        Json json = new Json();
        try {
            this.dataService.updateVData(pd);
            json.setSuccess(true);
            json.setMsg("修改信息成功");
        } catch (Exception e) {
            e.printStackTrace();
            String message = "修改信息失败";
            json.setSuccess(false);
            json.setMsg(message);
            log.error(message, e);
            throw new MyException(message);
        }
        this.writeJson(response,json);
    }

    /**
     * @title rejectAnyNod
     * @description 流程节点回退
     * @author Administrator
     * @updateTime 2022/5/12 0012 10:22
     */
    @PostMapping("/rejectAnyNod")
    public void rejectAnyNod(@Valid @RequestBody PageData pd,HttpServletResponse response) throws Exception {
        Json json = new Json();
        try {
            this.dataService.rejectAnyNod(pd);
            json.setSuccess(true);
            json.setMsg("流程退回成功");
        } catch (Exception e) {
            String message = "流程退回失败,"+e.getMessage();
            json.setSuccess(false);
            json.setMsg(message);
//            log.error(message, e);
//            throw new MyException(message);
        }
        this.writeJson(response,json);
    }

    /**
     * @title delegateTask
     * @description 任务委派
     * @author Administrator
     * @updateTime 2022/5/12 0012 10:22
     */
    @PostMapping("/delegateTask")
    public void delegateTask(@Valid @RequestBody PageData pd,HttpServletResponse response) throws Exception {
        Json json = new Json();
        try {
            this.dataService.delegateTask(pd);
            json.setSuccess(true);
            json.setMsg("任务委派成功");
        } catch (Exception e) {
            String message = "任务委派失败";
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
     * @author Administrator
     * @updateTime 2021/10/1 0001 22:28
     */
    @GetMapping("/delData")
    public void delData(HttpServletRequest request, HttpServletResponse response) throws Exception {
        PageData pd = new PageData(request);
        Json json = new Json();
        try {
            this.dataService.delData(pd);
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
     * @author Administrator
     * @updateTime 2021/10/1 0001 22:29
     */
    @PostMapping("/updateStatus")
    @PreAuthorize("hasAnyAuthority('vdata:status')")
    public void updateStatus(@Valid @RequestBody PageData pd,HttpServletResponse response) throws Exception {
        Json json = new Json();
        try {
            if(pd.get("status_type").equals("1")){//单启用
                PageData p = new PageData();
                p.put("status","1");
                p.put("update_time",DateTimeUtil.getDateTimeStr());
                p.put("table_name",pd.get("table_name"));
                this.dataService.updateStatus(p);
                p.put("status","0");
                p.put("id",pd.get("id"));
                this.dataService.updateStatus(p);
            }else if(pd.get("status_type").equals("2")){//多启用
                this.dataService.updateStatus(pd);
            }
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

    /**
     * @title findVFormData
     * @description findVFormData
     * @author Administrator
     * @updateTime 2022/5/9 0009 23:34
     */
    @GetMapping("/findVFormData")
    public void findVFormData(HttpServletRequest request, HttpServletResponse response) throws Exception {
        PageData pd = new PageData(request);
        Vform vform = vformService.getById(pd.get("table_id").toString());
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("table_id",vform.getId());
        List<Vfield> vfields = vfieldService.list(queryWrapper);
        vform.setVfields(vfields);
        this.writeJson(response,vform);
    }

}
