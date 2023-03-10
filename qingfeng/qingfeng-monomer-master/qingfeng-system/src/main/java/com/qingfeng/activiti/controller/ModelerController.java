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

    //????????????service????????????????????????????????????????????????????????????????????????
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
     * @Description: ????????????????????????
     * @Param: [page, request, response, session]
     * @return: void
     * @Author: anxingtao
     * @Date: 2020-8-8 17:42
     */
    @GetMapping("/findListPage")
    @PreAuthorize("hasAnyAuthority('modeler:info')")
    public void findListPage(Page page, HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException {
        PageData pd = new PageData(request);
        //????????????
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
        json.setMsg("?????????????????????");
        json.setCode(0);
        json.setData(list);
        json.setCount(num.intValue());
        json.setSuccess(true);
        this.writeJson(response,json);
    }


    /**
     * @Description: ??????????????????
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
        //?????????
        String name = pd.getString("name");
        //??????key
        String key = pd.getString("key");
        //????????????
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
            System.out.println("?????????moduleId:"+newModel.getId());
            json.setSuccess(true);
            json.setData(newModel);
            json.setMsg("?????????????????????");
//            return "redirect:/modeler/modeler.html?modelId="+newModel.getId();
        } catch (Exception e) {
            json.setSuccess(false);
            json.setMsg("?????????????????????");
            e.getStackTrace();
        }
        this.writeJson(response,json);
    }


    /**
     * @Description: publish????????????
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
                json.setMsg("?????????????????????????????????????????????????????????????????????????????????");
            }else{
                JsonNode modelNode = new ObjectMapper().readTree(bytes);
                BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);

                if (!CollectionUtils.isEmpty(model.getProcesses())) {
                    byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(model);
                    String processName = modelData.getName() + ".bpmn20.xml";
                    //????????????????????????????????????
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
                    json.setMsg("?????????????????????");
                }else{
                    json.setSuccess(false);
                    json.setMsg("?????????????????????????????????????????????????????????????????????????????????");
                }
            }

        } catch (Exception e) {
            json.setSuccess(false);
            json.setMsg("?????????????????????");
            e.getStackTrace();
        }
        this.writeJson(response,json);
    }


    /**
     * @Description: revokePublish????????????
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
                 * ????????????true:??????????????????????????????????????????????????????????????????????????????
                 * ?????????true:???????????????,????????????????????????????????????????????????????????????
                 */
                repositoryService.deleteDeployment(modelData.getDeploymentId(),true);
                json.setSuccess(true);
                json.setMsg("?????????????????????");
            } catch (Exception e) {
                json.setSuccess(false);
                json.setMsg("?????????????????????");
                e.getStackTrace();
            }
        }
        this.writeJson(response,json);
    }


    /**
     * @Description: exportModeler????????????bpmn??????
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

            // ??????????????????
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
     * @Description: findFlowchart ??????????????????????????????json ??????
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
     * @Description: del ??????
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
        json.setMsg("???????????????");
        this.writeJson(response,json);
    }




    //===============================????????????=====================================
    /**
     * @title findNodes
     * @description ????????????????????????
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
                    if (e instanceof org.activiti.bpmn.model.StartEvent) { //????????????
//                        System.out.println("flowelement id:" + e.getId() + "  name:" + e.getName() + "   class:" + e.getClass().toString());
                        PageData p = new PageData();
                        p.put("node_key",e.getId());
                        p.put("node_name",e.getName());
                        p.put("node_type","StartEvent");
                        pd.put("node_key",e.getId());
                        //???????????????????????????
                        PageData assignmentPd = assignmentService.findActivitiAssignment(pd);
                        p.put("assignmentPd",assignmentPd);
                        list.add(p);
                    }else if (e instanceof org.activiti.bpmn.model.UserTask) { //????????????
//                        System.out.println("flowelement id:" + e.getId() + "  name:" + e.getName() + "   class:" + e.getClass().toString());
                        PageData p = new PageData();
                        p.put("node_key",e.getId());
                        p.put("node_name",e.getName());
                        p.put("node_type","UserTask");
                        pd.put("node_key",e.getId());
                        //???????????????????????????
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
     * @description ??????????????????
     * @author Administrator
     * @updateTime 2021/9/5 0005 16:36
     */
    @GetMapping("/findAssignment")
//    @PreAuthorize("hasAnyAuthority('modeler:nodes')")
    public void toAssignment(HttpServletRequest request,HttpServletResponse response) throws Exception {
        PageData pd = new PageData(request);
        Json json = new Json();
        //?????????????????????
        Group group = new Group();
        List<Group> groupList = groupService.findList(group);
        json.setObject(groupList);
        //???????????????????????????
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
        json.setMsg("????????????");
        json.setSuccess(true);
        this.writeJson(response,json);
    }

    /**
     * @title saveAssignment
     * @description ???????????????????????????
     * @author Administrator
     * @updateTime 2021/9/5 0005 16:37
     */
    @PostMapping("/saveAssignment")
    public void saveAssignment(@Valid @RequestBody Assignment assignment , HttpServletResponse response) throws IOException  {
        String time = DateTimeUtil.getDateTimeStr();
        //??????????????????????????????
        String user_id = "1";
        String organize_id = "1";

        if(Verify.verifyIsNotNull(assignment.getId())){
            assignment.setUpdate_time(time);
            assignment.setUpdate_user(user_id);
            assignmentService.updateById(assignment);
        }else{
            //??????id
            String id = GuidUtil.getUuid();
            assignment.setId(id);
            assignment.setCreate_time(time);
            assignment.setCreate_user(user_id);
            assignment.setCreate_organize(organize_id);
            assignmentService.save(assignment);
        }
        Json json = new Json();
        json.setSuccess(true);
        json.setMsg("???????????????");
        this.writeJson(response,json);
    }

    //=======================================Bpmnjs???????????????=============================================

    /**
     * @title deployByString
     * @description ????????????
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
        json.setMsg("???????????????");
        this.writeJson(response,json);
    }


    /**
     * @title findUserOrOrganizeNames
     * @description ???????????????????????????????????????????????????
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
        if(type.equals("assignee")){//?????????
            pd.put("user_ids", Arrays.asList(ids));
            List<PageData> list = userService.findUserList(pd);
            for (PageData p:list) {
                myIds.append(p.get("id")).append(",");
                myNames.append(p.get("name")).append(",");
            }
        }else if(type.equals("candidateUsers")){//?????????
            pd.put("user_ids",Arrays.asList(ids));
            List<PageData> list = userService.findUserList(pd);
            for (PageData p:list) {
                myIds.append(p.get("id")).append(",");
                myNames.append(p.get("name")).append(",");
            }
        }else if(type.equals("candidateGroups")){//?????????
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
        json.setMsg("???????????????");
        this.writeJson(response,json);
    }

    /**
     * @title findGroupList
     * @description ???????????????????????????
     * @author Administrator
     * @updateTime 2021/9/11 0011 14:51
     */
    @GetMapping("/findGroupList")
    public void findGroupList(QueryRequest queryRequest, Group group, HttpServletResponse response) throws Exception {
        //??????????????????
        List<Group> list = groupService.findList(group);
        Json json = new Json();
        json.setMsg("?????????????????????");
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
        json.setMsg("?????????????????????");
        json.setData(data);
        json.setSuccess(true);
        this.writeJson(response,json);
    }


    //=========================?????????????????????????????????============================
    /**
     * @title findFormList
     * @description ??????????????????
     * @author Administrator
     * @updateTime 2022/5/8 0008 16:46
     */
    @GetMapping("/findFormList")
    public void findFormList(HttpServletRequest request, HttpServletResponse response) throws Exception {
        PageData pd = new PageData(request);
        //????????????????????????
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("type","0");
        if(Verify.verifyIsNotNull(pd.get("table_id"))){
            Vform vform = vformService.getById(pd.get("table_id").toString());
            queryWrapper.eq("table_name",vform.getTable_name());
//            queryWrapper.ne("id",pd.get("table_id").toString());
        }
        List<PageData> list = vformService.list(queryWrapper);
        Json json = new Json();
        json.setMsg("?????????????????????");
        json.setData(list);
        json.setSuccess(true);
        this.writeJson(response,json);
    }

    @GetMapping("/findFieldList")
    public void findFieldList(HttpServletRequest request, HttpServletResponse response) throws Exception {
        PageData pd = new PageData(request);
        Json json = new Json();
        //????????????????????????
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

            json.setMsg("?????????????????????");
            json.setData(childFormList);
            json.setObject(vform);
            json.setSuccess(true);
        }else{
            json.setMsg("???????????????????????????");
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
        json.setMsg("?????????????????????");
        json.setSuccess(true);
        this.writeJson(response,json);
    }


}
