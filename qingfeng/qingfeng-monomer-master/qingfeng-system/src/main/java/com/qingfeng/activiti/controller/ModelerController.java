package com.qingfeng.activiti.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.qingfeng.activiti.service.IAssignmentService;
import com.qingfeng.base.controller.BaseController;
import com.qingfeng.customize.service.IVfieldService;
import com.qingfeng.customize.service.IVformService;
import com.qingfeng.entity.QueryRequest;
import com.qingfeng.entity.activiti.Assignment;
import com.qingfeng.entity.customize.Vfield;
import com.qingfeng.entity.customize.Vform;
import com.qingfeng.entity.system.Group;
import com.qingfeng.system.service.IGroupService;
import com.qingfeng.system.service.IOrganizeService;
import com.qingfeng.system.service.IUserService;
import com.qingfeng.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.ModelMap;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.activiti.editor.constants.ModelDataJsonConstants.MODEL_DESCRIPTION;
import static org.activiti.editor.constants.ModelDataJsonConstants.MODEL_NAME;

/**
 * Created by anxingtao on 2020-8-8.
 */
@Slf4j
@Validated
@RestController
@RequestMapping("activiti/modeler")
public class ModelerController extends BaseController {

    //流程仓储service、用于管理流程仓库，例如部署、删除、读取流程资源
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private IAssignmentService assignmentService;
    @Autowired
    private IGroupService groupService;
    @Autowired
    private ProcessRuntime processRuntime;
    @Autowired
    private IUserService userService;
    @Autowired
    private IOrganizeService organizeService;
    @Autowired
    private IVformService vformService;
    @Autowired
    public IVfieldService vfieldService;


    /**
     * @Description: 查询流程部署列表
     * @Param: [page, request, response, session]
     * @return: void
     * @Author: anxingtao
     * @Date: 2020-8-8 17:42
     */
    @GetMapping("/findListPage")
    @PreAuthorize("hasAnyAuthority('modeler:info')")
    public void findListPage(Page page, HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException {
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
        List<Model> list = repositoryService.createModelQuery().orderByCreateTime().desc().listPage(firstResult,page.getShowCount());
        Long num = repositoryService.createModelQuery().count();
        Json json = new Json();
        json.setMsg("获取数据成功。");
        json.setCode(0);
        json.setData(list);
        json.setCount(num.intValue());
        json.setSuccess(true);
        this.writeJson(response,json);
    }


    /**
     * @Description: 创建流程模型
     * @Param: [map, request, response]
     * @return: java.lang.String
     * @Author: anxingtao
     * @Date: 2020-8-8 17:39
     */
    @GetMapping("/save")
    @PreAuthorize("hasAnyAuthority('modeler:add')")
    public void save(ModelMap map, HttpServletRequest request, HttpServletResponse response) throws IOException {
        PageData pd = new PageData(request);
        Json json = new Json();
        //模型名
        String name = pd.getString("name");
        //模型key
        String key = pd.getString("key");
        //模型描述
        String description = pd.getString("description");
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode editorNode = objectMapper.createObjectNode();
            editorNode.put("id", "canvas");
            editorNode.put("resourceId", "canvas");

            ObjectNode stencilSetNode = objectMapper.createObjectNode();
            stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");

            editorNode.put("stencilset", stencilSetNode);

            ObjectNode modelObjectNode = objectMapper.createObjectNode();
            modelObjectNode.put(MODEL_NAME, name);
            modelObjectNode.put(ModelDataJsonConstants.MODEL_REVISION, 1);
            description = StringUtils.defaultString(description);
            modelObjectNode.put(MODEL_DESCRIPTION, description);

            Model newModel = repositoryService.newModel();
            newModel.setMetaInfo(modelObjectNode.toString());
            newModel.setName(name);
            newModel.setKey(StringUtils.defaultString(key));

            repositoryService.saveModel(newModel);
            repositoryService.addModelEditorSource(newModel.getId(), editorNode.toString().getBytes("utf-8"));
            System.out.println("生成的moduleId:"+newModel.getId());
            json.setSuccess(true);
            json.setData(newModel);
            json.setMsg("创建模型成功。");
//            return "redirect:/modeler/modeler.html?modelId="+newModel.getId();
        } catch (Exception e) {
            json.setSuccess(false);
            json.setMsg("创建模型失败。");
            e.getStackTrace();
        }
        this.writeJson(response,json);
    }


    /**
     * @Description: publish流程发布
     * @Param: [request, response, session]
     * @return: void
     * @Author: anxingtao
     * @Date: 2020-8-9 1:22
     */
    @GetMapping("/publish")
    @PreAuthorize("hasAnyAuthority('modeler:publish')")
    public void publish(HttpServletRequest request,HttpServletResponse response,HttpSession session) throws IOException  {
        PageData pd = new PageData(request);
        Json json = new Json();
        try {
            Model modelData = repositoryService.getModel(pd.get("modelId").toString());
            byte[] bytes = repositoryService.getModelEditorSource(modelData.getId());
            if (bytes == null) {
                json.setSuccess(false);
                json.setMsg("流程模型数据为空，请先设计流程并成功保存，再进行发布。");
            }else{
                JsonNode modelNode = new ObjectMapper().readTree(bytes);
                BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);

                if (!CollectionUtils.isEmpty(model.getProcesses())) {
                    byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(model);
                    String processName = modelData.getName() + ".bpmn20.xml";
                    //处理部署流程无法生成图片
                    byte[] bytesPng = repositoryService.getModelEditorSourceExtra(modelData.getId());
                    String processPngName = modelData.getName() + ".bpmn20.png";
                    Deployment deployment = repositoryService.createDeployment()
                            .name(modelData.getName())
                            .key(modelData.getKey())
                            .addString(processName, new String(bpmnBytes, "UTF-8"))
//                            .addString(processPngName, new String(bytesPng, "UTF-8"))
                            .addBytes(processPngName, bytesPng)
                            .deploy();
//                Deployment deployment = repositoryService.createDeployment()
//                        .name(modelData.getName())
//                        .addBpmnModel(processName,model).deploy();
                    modelData.setDeploymentId(deployment.getId());
                    repositoryService.saveModel(modelData);
                    json.setSuccess(true);
                    json.setMsg("流程部署成功。");
                }else{
                    json.setSuccess(false);
                    json.setMsg("流程模型数据为空，请先设计流程并成功保存，再进行发布。");
                }
            }

        } catch (Exception e) {
            json.setSuccess(false);
            json.setMsg("流程部署失败。");
            e.getStackTrace();
        }
        this.writeJson(response,json);
    }


    /**
     * @Description: revokePublish流程撤销
     * @Param: [request, response, session]
     * @return: void
     * @Author: anxingtao
     * @Date: 2020-8-9 1:22
     */
    @GetMapping("/revokePublish")
    @PreAuthorize("hasAnyAuthority('modeler:revokePublish')")
    public void revokePublish(HttpServletRequest request,HttpServletResponse response,HttpSession session) throws IOException  {
        PageData pd = new PageData(request);
        Json json = new Json();
        Model modelData = repositoryService.getModel(pd.get("modelId").toString());
        if(Verify.verifyIsNotNull(modelData)){
            try {
                /**
                 * 参数不加true:为普通删除，如果当前规则下有正在执行的流程，则抛异常
                 * 参数加true:为级联删除,会删除和当前规则相关的所有信息，包括历史
                 */
                repositoryService.deleteDeployment(modelData.getDeploymentId(),true);
                json.setSuccess(true);
                json.setMsg("流程撤销成功。");
            } catch (Exception e) {
                json.setSuccess(false);
                json.setMsg("流程撤销失败。");
                e.getStackTrace();
            }
        }
        this.writeJson(response,json);
    }


    /**
     * @Description: exportModeler导出流程bpmn文件
     * @Param: [request, response, session]
     * @return: void
     * @Author: anxingtao
     * @Date: 2020-8-9 17:59
     */
    @GetMapping("/downloadModeler")
//    @PreAuthorize("hasAnyAuthority('modeler:download')")
    public void exportModeler(HttpServletRequest request,HttpServletResponse response,HttpSession session) throws IOException  {
        PageData pd = new PageData(request);
        Json json = new Json();
        try {
            Model modelData = repositoryService.getModel(pd.get("modelId").toString());
            BpmnJsonConverter jsonConverter = new BpmnJsonConverter();
            JsonNode editorNode = new ObjectMapper().readTree(repositoryService.getModelEditorSource(modelData.getId()));
            BpmnModel bpmnModel = jsonConverter.convertToBpmnModel(editorNode);

            // 流程非空判断
            if (!CollectionUtils.isEmpty(bpmnModel.getProcesses())) {
                BpmnXMLConverter xmlConverter = new BpmnXMLConverter();
                byte[] bpmnBytes = xmlConverter.convertToXML(bpmnModel);

                ByteArrayInputStream in = new ByteArrayInputStream(bpmnBytes);
                String filename = bpmnModel.getMainProcess().getId() + ".bpmn";
                response.setHeader("Content-Disposition", "attachment; filename=" + filename);
                IOUtils.copy(in, response.getOutputStream());
                response.flushBuffer();
            } else {
            }
        } catch (Exception e) {
            e.getStackTrace();
        }
    }


    /**
     * @Description: findFlowchart 获取流程图信息，通过json 返回
     * @Param: [map, request]
     * @return: java.lang.String
     * @Author: anxingtao
     * @Date: 2020-11-11 11:28
     */
    @GetMapping("/findFlowchart")
//    @PreAuthorize("hasAnyAuthority('modeler:flowchart')")
    public void findFlowchart(HttpServletRequest request,HttpServletResponse respose) throws Exception {
        PageData pd = new PageData(request);
        String deploymentId=pd.getString("deploymentId");
        List<String> names=repositoryService.getDeploymentResourceNames(deploymentId);
        String imageName=null;
        for(String name:names){
            if(name.indexOf(".png")>=0){
                imageName=name;
            }
        }
        InputStream in=null;
        if(imageName!=null){
            in=repositoryService.getResourceAsStream(deploymentId,imageName);
        }
        OutputStream outputStream = respose.getOutputStream();
        byte[] bytes = new byte[1024];
        int rc = 0;
        while ((rc = in.read(bytes, 0, 100)) > 0) {
            outputStream.write(bytes, 0, rc);
        }
        in.close();
        outputStream.close();
    }


    /**
     * @Description: del 删除
     * @Param: [request, response, session]
     * @return: void
     * @Author: anxingtao
     * @Date: 2020-8-8 19:59
     */
    @DeleteMapping("/{ids}")
    @PreAuthorize("hasAnyAuthority('modeler:del')")
    public void delete(@NotBlank(message = "{required}") @PathVariable String ids, HttpServletResponse response) throws Exception {
        String[] del_ids = ids.split(StringPool.COMMA);
        for (String id:del_ids) {
            repositoryService.deleteModel(id);
        }
        Json json = new Json();
        json.setSuccess(true);
        json.setMsg("操作成功。");
        this.writeJson(response,json);
    }




    //===============================节点管理=====================================
    /**
     * @title findNodes
     * @description 查询流程审批节点
     * @author Administrator
     * @updateTime 2021/9/5 0005 16:36
     */
    @GetMapping("/findNodes")
//    @PreAuthorize("hasAnyAuthority('modeler:nodes')")
    public void findNodes(HttpServletRequest request,HttpServletResponse response) throws IOException  {
        PageData pd = new PageData(request);
        Model modelData = repositoryService.getModel(pd.get("model_id").toString());
        BpmnJsonConverter jsonConverter = new BpmnJsonConverter();
        JsonNode editorNode = new ObjectMapper().readTree(repositoryService.getModelEditorSource(modelData.getId()));
        BpmnModel bpmnModel = jsonConverter.convertToBpmnModel(editorNode);
        List<PageData> list = new ArrayList<PageData>();
        if(Verify.verifyIsNotNull(bpmnModel)) {
            if(Verify.verifyIsNotNull(bpmnModel.getMainProcess())){
                Collection<FlowElement> flowElements = bpmnModel.getMainProcess().getFlowElements();
                for (FlowElement e : flowElements) {
                    if (e instanceof org.activiti.bpmn.model.StartEvent) { //开始节点
//                        System.out.println("flowelement id:" + e.getId() + "  name:" + e.getName() + "   class:" + e.getClass().toString());
                        PageData p = new PageData();
                        p.put("node_key",e.getId());
                        p.put("node_name",e.getName());
                        p.put("node_type","StartEvent");
                        pd.put("node_key",e.getId());
                        //查询指定办理人信息
                        PageData assignmentPd = assignmentService.findActivitiAssignment(pd);
                        p.put("assignmentPd",assignmentPd);
                        list.add(p);
                    }else if (e instanceof org.activiti.bpmn.model.UserTask) { //用户节点
//                        System.out.println("flowelement id:" + e.getId() + "  name:" + e.getName() + "   class:" + e.getClass().toString());
                        PageData p = new PageData();
                        p.put("node_key",e.getId());
                        p.put("node_name",e.getName());
                        p.put("node_type","UserTask");
                        pd.put("node_key",e.getId());
                        //查询指定办理人信息
                        PageData assignmentPd = assignmentService.findActivitiAssignment(pd);
                        p.put("assignmentPd",assignmentPd);
                        list.add(p);
                    }
                }
            }
        }
        this.writeJson(response,list);
    }


    /**
     * @title findAssignment
     * @description 节点办理设置
     * @author Administrator
     * @updateTime 2021/9/5 0005 16:36
     */
    @GetMapping("/findAssignment")
//    @PreAuthorize("hasAnyAuthority('modeler:nodes')")
    public void toAssignment(HttpServletRequest request,HttpServletResponse response) throws Exception {
        PageData pd = new PageData(request);
        Json json = new Json();
        //查询用户组信息
        Group group = new Group();
        List<Group> groupList = groupService.findList(group);
        json.setObject(groupList);
        //查询指定办理人信息
        PageData assignmentPd = assignmentService.findActivitiAssignment(pd);
        if(Verify.verifyIsNotNull(assignmentPd)){
            String assign_mode = assignmentPd.get("assign_mode").toString();
            if(Verify.verifyIsNotNull(assignmentPd.get("assign_content"))){
                String assign_content = assignmentPd.get("assign_content").toString();
                if(assign_mode.equals("1")){
                    assignmentPd.put("organize_id",assign_content.split("#")[0]);
                    assignmentPd.put("organize_name",assign_content.split("#")[1]);
                }else if(assign_mode.equals("8")||assign_mode.equals("10")){
                    assignmentPd.put("user_ids",assign_content.split("#")[0]);
                    assignmentPd.put("user_names",assign_content.split("#")[1]);
                }else if(assign_mode.equals("9")){
                    assignmentPd.put("user_id",assign_content.split("#")[0]);
                    assignmentPd.put("user_name",assign_content.split("#")[1]);
                }else if(assign_mode.equals("11")){
                    assignmentPd.put("organize_ids",assign_content.split("#")[0]);
                    assignmentPd.put("organize_names",assign_content.split("#")[1]);
                }
            }
        }
        json.setData(assignmentPd);
        json.setMsg("查询成功");
        json.setSuccess(true);
        this.writeJson(response,json);
    }

    /**
     * @title saveAssignment
     * @description 保存流程节点办理人
     * @author Administrator
     * @updateTime 2021/9/5 0005 16:37
     */
    @PostMapping("/saveAssignment")
    public void saveAssignment(@Valid @RequestBody Assignment assignment , HttpServletResponse response) throws IOException  {
        String time = DateTimeUtil.getDateTimeStr();
        //获取当前登录用户信息
        String user_id = "1";
        String organize_id = "1";

        if(Verify.verifyIsNotNull(assignment.getId())){
            assignment.setUpdate_time(time);
            assignment.setUpdate_user(user_id);
            assignmentService.updateById(assignment);
        }else{
            //主键id
            String id = GuidUtil.getUuid();
            assignment.setId(id);
            assignment.setCreate_time(time);
            assignment.setCreate_user(user_id);
            assignment.setCreate_organize(organize_id);
            assignmentService.save(assignment);
        }
        Json json = new Json();
        json.setSuccess(true);
        json.setMsg("操作成功。");
        this.writeJson(response,json);
    }

    //=======================================Bpmnjs流程设计器=============================================

    /**
     * @title deployByString
     * @description 流程部署
     * @author Administrator
     * @updateTime 2021/9/11 0011 9:53
     */
    @PostMapping("/deployByString")
    public void deployByString(@RequestBody PageData pd, HttpServletResponse response) throws IOException  {
        System.out.println(pd);
        String xmlByte = pd.get("xmlString").toString();

        String processName =  "deploy"+GuidUtil.getCodeNum()+".bpmn20.xml";
        repositoryService.createDeployment()
                .name("deploy"+GuidUtil.getCodeNum())
                .addString(processName, xmlByte)
                .deploy();

        Json json = new Json();
        json.setSuccess(true);
        json.setMsg("操作成功。");
        this.writeJson(response,json);
    }


    /**
     * @title findUserOrOrganizeNames
     * @description 流程办理人、候选人、候选组名称查询
     * @author Administrator
     * @updateTime 2021/9/11 0011 9:53
     */
    @PostMapping("/findUserOrOrganizeNames")
    public void findUserOrOrganizeNames(@RequestBody PageData pd, HttpServletResponse response) throws IOException  {
        System.out.println(pd);

        String type = pd.get("type").toString();
        String[] ids = pd.get("ids").toString().split(",");
        StringBuilder myIds = new StringBuilder();
        StringBuilder myNames = new StringBuilder();
        if(type.equals("assignee")){//办理人
            pd.put("user_ids", Arrays.asList(ids));
            List<PageData> list = userService.findUserList(pd);
            for (PageData p:list) {
                myIds.append(p.get("id")).append(",");
                myNames.append(p.get("name")).append(",");
            }
        }else if(type.equals("candidateUsers")){//候选人
            pd.put("user_ids",Arrays.asList(ids));
            List<PageData> list = userService.findUserList(pd);
            for (PageData p:list) {
                myIds.append(p.get("id")).append(",");
                myNames.append(p.get("name")).append(",");
            }
        }else if(type.equals("candidateGroups")){//候选组
            pd.put("organize_ids",Arrays.asList(ids));
            List<PageData> list = organizeService.findOrganizeList(pd);
            for (PageData p:list) {
                myIds.append(p.get("id")).append(",");
                myNames.append(p.get("name")).append(",");
            }
        }
        if(myIds.length()>0){
            pd.put("myIds",myIds.substring(0,myIds.length()-1));
            pd.put("myNames",myNames.substring(0,myNames.length()-1));
        }else{
            pd.put("myIds",myIds);
            pd.put("myNames",myNames);
        }
        Json json = new Json();
        json.setSuccess(true);
        json.setData(pd);
        json.setMsg("操作成功。");
        this.writeJson(response,json);
    }

    /**
     * @title findGroupList
     * @description 查询用户组列表信息
     * @author Administrator
     * @updateTime 2021/9/11 0011 14:51
     */
    @GetMapping("/findGroupList")
    public void findGroupList(QueryRequest queryRequest, Group group, HttpServletResponse response) throws Exception {
        //处理数据权限
        List<Group> list = groupService.findList(group);
        Json json = new Json();
        json.setMsg("获取数据成功。");
        json.setData(list);
        json.setSuccess(true);
        this.writeJson(response,json);
    }


    @GetMapping("/findActivitiAssignment")
    public void findActivitiAssignment(Assignment assignment, HttpServletResponse response) throws IOException  {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("model_id",assignment.getModel_id());
        queryWrapper.eq("node_key",assignment.getNode_key());
        queryWrapper.orderByDesc("create_time");
        Assignment data = assignmentService.getOne(queryWrapper);
        Json json = new Json();
        json.setMsg("获取数据成功。");
        json.setData(data);
        json.setSuccess(true);
        this.writeJson(response,json);
    }


    //=========================新增工作流节点指定表单============================
    /**
     * @title findFormList
     * @description 查询表单列表
     * @author Administrator
     * @updateTime 2022/5/8 0008 16:46
     */
    @GetMapping("/findFormList")
    public void findFormList(HttpServletRequest request, HttpServletResponse response) throws Exception {
        PageData pd = new PageData(request);
        //查询表单列表信息
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("type","0");
        if(Verify.verifyIsNotNull(pd.get("table_id"))){
            Vform vform = vformService.getById(pd.get("table_id").toString());
            queryWrapper.eq("table_name",vform.getTable_name());
//            queryWrapper.ne("id",pd.get("table_id").toString());
        }
        List<PageData> list = vformService.list(queryWrapper);
        Json json = new Json();
        json.setMsg("获取数据成功。");
        json.setData(list);
        json.setSuccess(true);
        this.writeJson(response,json);
    }

    @GetMapping("/findFieldList")
    public void findFieldList(HttpServletRequest request, HttpServletResponse response) throws Exception {
        PageData pd = new PageData(request);
        Json json = new Json();
        //查询表单字段信息
        if(Verify.verifyIsNotNull(pd.get("table_id"))){
            Vform vform = vformService.getById(pd.get("table_id").toString());
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("table_id",pd.get("table_id").toString());
            List<Vfield> list = vfieldService.list(queryWrapper);
            vform.setVfields(list);

            queryWrapper = new QueryWrapper();
            queryWrapper.eq("main_id",pd.get("table_id").toString());
            queryWrapper.eq("type","1");
            List<Vform> childFormList = vformService.list(queryWrapper);
            for (Vform v:childFormList) {
                queryWrapper = new QueryWrapper();
                queryWrapper.eq("table_id",v.getId());
                List<Vfield> ls = vfieldService.list(queryWrapper);
                v.setVfields(ls);
            }

            json.setMsg("获取数据成功。");
            json.setData(childFormList);
            json.setObject(vform);
            json.setSuccess(true);
        }else{
            json.setMsg("表单信息不可为空。");
            json.setSuccess(false);
        }
        this.writeJson(response,json);
    }


    @GetMapping("/findProcessField")
    public void findProcessField(HttpServletRequest request, HttpServletResponse response) throws IOException  {
        PageData pd = new PageData(request);
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(pd.get("process_key").toString()).latestVersion().singleResult();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinition.getId());
        System.out.println("-------------------findProcessField-------------------");
        System.out.println(bpmnModel.getMainProcess().getName());
        System.out.println(bpmnModel.getMainProcess().getId());

        System.out.println(bpmnModel.getStartFormKey(processDefinition.getId()));

        Json json = new Json();
        json.setMsg("获取数据成功。");
        json.setSuccess(true);
        this.writeJson(response,json);
    }


}
