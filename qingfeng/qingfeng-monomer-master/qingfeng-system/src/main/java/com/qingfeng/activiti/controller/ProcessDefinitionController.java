package com.qingfeng.activiti.controller;

import cn.hutool.core.bean.BeanUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.qingfeng.activiti.service.ProcessDefinitionService;
import com.qingfeng.activiti.service.ProcessService;
import com.qingfeng.base.controller.BaseController;
import com.qingfeng.utils.*;
import com.qingfeng.utils.upload.ParaUtil;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.CollectionUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotBlank;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @version 1.0.0
 * @ProjectName qingfeng-cloud
 * @Description ????????????
 * @createTime 2021???08???05??? 23:13:00
 */
@Slf4j
@Validated
@Controller
@RequestMapping(value = "/activiti/processDefinition")
public class ProcessDefinitionController extends BaseController {

    @Autowired
    private ProcessDefinitionService processDefinitionService;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private ProcessService processService;

    /**
     * @Description: findListPage ????????????????????????
     * @Param: [page, request, response, session]
     * @return: void
     * @Author: anxingtao
     * @Date: 2020-8-15 11:32
     */
    @GetMapping("/findListPage")
    @PreAuthorize("hasAnyAuthority('processDefinition:info')")
    public void findListPage(Page page, HttpServletRequest request, HttpServletResponse response) throws IOException {
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

        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();//??????????????????????????????
        List<ProcessDefinition> processDefinitionList = null;
        if (Verify.verifyIsNotNull(pd.get("processDefinitionKey"))) {
            processDefinitionQuery = processDefinitionQuery.processDefinitionKeyLike(pd.get("processDefinitionKey").toString());//??????????????????Key??????
        }
        if (Verify.verifyIsNotNull(pd.get("processDefinitionName"))) {
            processDefinitionQuery = processDefinitionQuery.processDefinitionNameLike(pd.get("processDefinitionName").toString());//??????????????????name??????
        }
        processDefinitionQuery = processDefinitionQuery.latestVersion().orderByProcessDefinitionName().desc()//???????????????????????????????????????
                .orderByProcessDefinitionVersion().desc();//???????????????????????????
/*
        int firstResult = (pageNum-1) * pageSize;
        int maxResults = pageSize;
*/
        int firstResult = (page.getIndex()-1)*page.getShowCount();
        processDefinitionList = processDefinitionQuery.listPage(firstResult,page.getShowCount());
        //???????????????
        int total = (int) processDefinitionQuery.count();
        List<Map<String ,Object>> allList = new ArrayList<>();
        for (ProcessDefinition processDefinition : processDefinitionList) {
//            Map<String ,Object> map = new HashMap<>();
//            System.out.println("#######:"+processDefinition.getId());
//            String deploymentId = processDefinition.getDeploymentId();
//            Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(deploymentId).singleResult();
//            map.put("processDefinition", PageData.getInstance().objToPd(processDefinition));
//            map.put("deployment",PageData.getInstance().objToPd(deployment));
            PageData p = PageData.getInstance().objToPd(processDefinition);
            p.put("id",processDefinition.getId());
            allList.add(p);
        }
        System.out.println("=======================================");
        System.out.println(allList);
        Json json = new Json();
        json.setMsg("?????????????????????");
        json.setData(allList);
        json.setCount(total);
        json.setSuccess(true);
        this.writeJson(response,json);
    }


    /**
     * @Description: upload ??????????????????????????????????????????zip???bar???bpmn???
     * @Param: [file, request, response]
     * @return: void
     * @Author: anxingtao
     * @Date: 2020-8-11 22:31
     */
    @PostMapping("/uploadFile")
    @PreAuthorize("hasAnyAuthority('processDefinition:import')")
    public void uploadFile(MultipartFile file, HttpServletRequest request, HttpServletResponse response) throws IOException {
        PageData pd = new PageData(request);
        Json json = new Json();
        if (!file.isEmpty()) {
            String extensionName = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.') + 1);
            if (!"bpmn".equalsIgnoreCase(extensionName)
                    && !"zip".equalsIgnoreCase(extensionName)
                    && !"bar".equalsIgnoreCase(extensionName)) {
                json.setSuccess(false);
                json.setMsg("??????????????????????????? bpmn, zip ??? bar ?????????");
            }else{
                //????????????
                String savePath = ParaUtil.localName;
                String path = ParaUtil.activiti+ ParaUtil.resource+ DateTimeUtil.getDate()+"/"+ GuidUtil.getGuid()+"."+extensionName;
                File files = new File(savePath+path);
                if (!files.getParentFile().exists()){
                    files.getParentFile().mkdirs();
                }
                FileCopyUtils.copy(file.getInputStream(), Files.newOutputStream(files.toPath()));
                processDefinitionService.deployProcessDefinition(savePath+path);
                json.setSuccess(true);
                json.setMsg("?????????????????????????????????");
            }
        }else{
            json.setSuccess(false);
            json.setMsg("??????????????????????????????????????????");
        }
        this.writeJson(response,json);

    }


    /**
     * @Description: convertToModel ???????????????????????????
     * @Param: [file, request, response]
     * @return: void
     * @Author: anxingtao
     * @Date: 2020-8-15 15:23
     */
    @PostMapping("/convertToModel")
    @PreAuthorize("hasAnyAuthority('processDefinition:convertToModel')")
    public void convertToModel(@RequestBody PageData pd,HttpServletResponse response) throws Exception {
        System.out.println("-------convertToModel--------");
        System.out.println(pd.toString());
        Json json = new Json();
        //??????????????????
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(pd.get("processDefinitionId").toString()).singleResult();
        //??????bpmnStream
        InputStream bpmnStream = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(),
                processDefinition.getResourceName());

        XMLInputFactory xif = XMLInputFactory.newInstance();
        InputStreamReader in = new InputStreamReader(bpmnStream, "UTF-8");
        XMLStreamReader xtr = xif.createXMLStreamReader(in);
        BpmnModel bpmnModel = new BpmnXMLConverter().convertToBpmnModel(xtr);

        BpmnJsonConverter converter = new BpmnJsonConverter();
        ObjectNode modelNode = converter.convertToJson(bpmnModel);
        Model modelData = repositoryService.newModel();
        modelData.setKey(processDefinition.getKey());
        modelData.setName(processDefinition.getResourceName());
        modelData.setCategory(processDefinition.getDeploymentId());

        ObjectNode modelObjectNode = new ObjectMapper().createObjectNode();
        modelObjectNode.put(ModelDataJsonConstants.MODEL_NAME, processDefinition.getName());
        modelObjectNode.put(ModelDataJsonConstants.MODEL_REVISION, 1);
        modelObjectNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, processDefinition.getDescription());
        modelData.setMetaInfo(modelObjectNode.toString());
        repositoryService.saveModel(modelData);
        repositoryService.addModelEditorSource(modelData.getId(), modelNode.toString().getBytes("utf-8"));

        json.setSuccess(true);
        json.setMsg("?????????????????????????????????");
        this.writeJson(response,json);
    }

    /**
     * @Description: del??????????????????????????????
     * @Param: [request, response, session]
     * @return: void
     * @Author: anxingtao
     * @Date: 2020-8-15 15:32
     */
    @DeleteMapping("/{deploymentId}")
    @PreAuthorize("hasAnyAuthority('processDefinition:del')")
    public void del(@NotBlank(message = "{required}") @PathVariable String deploymentId,HttpServletRequest request,HttpServletResponse response) throws IOException  {
        PageData pd = new PageData(request);
        Json json = new Json();
        List<ProcessInstance> instanceList = runtimeService.createProcessInstanceQuery()
                .deploymentId(deploymentId)
                .list();
        if (!CollectionUtils.isEmpty(instanceList)) {
            // ?????????????????????????????????
            json.setMsg("?????????????????????????????????????????????");
            json.setSuccess(false);
        }else{
            repositoryService.deleteDeployment(deploymentId, true); // true ????????????????????????????????? act_ru_execution ??????
            json.setSuccess(true);
            json.setMsg("???????????????");
        }
        this.writeJson(response,json);
    }



    /**
     * @Description: findProcessDefinition ????????????????????????
     * @Param: [page, map, request, response]
     * @return: void
     * @Author: anxingtao
     * @Date: 2020-8-18 23:31
     */
    @PostMapping("/findProcessDefinition")
    @PreAuthorize("hasAnyAuthority('processDefinition:findProcessDefinition')")
    public void findProcessDefinition(Page page, ModelMap map, HttpServletRequest request, HttpServletResponse response) throws Exception {
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
        Page list = processDefinitionService.findProcessDefinition(page.getIndex(),page.getShowCount(),pd.get("processDefinitionKey").toString(),pd.get("processDefinitionName").toString());
        Json json = new Json();
        json.setMsg("?????????????????????");
        json.setData(list);
        json.setSuccess(true);
        this.writeJson(response,json);
    }


    //=========================??????????????????==================================

    /**
     * @Description: findHistoryProcessDefinitionPage ????????????????????????
     * @Param: [page, request, response, session]
     * @return: void
     * @Author: anxingtao
     * @Date: 2020-9-9 10:05
     */
    @GetMapping("/findHistoryProcessDefinitionPage")
    @PreAuthorize("hasAnyAuthority('processDefinition:history')")
    public void findHistoryProcessDefinitionPage(Page page, HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException {
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

        ProcessDefinitionQuery pq = repositoryService.createProcessDefinitionQuery();//??????????????????????????????
        int version = pq.processDefinitionKey(pd.get("key").toString()).latestVersion().singleResult().getVersion();
        System.out.println("#############:"+version);
        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();//??????????????????????????????
        if (Verify.verifyIsNotNull(pd.get("processDefinitionKey"))) {
            processDefinitionQuery = processDefinitionQuery.processDefinitionKeyLike(pd.get("processDefinitionKey").toString());//??????????????????Key??????
        }
        if (Verify.verifyIsNotNull(pd.get("processDefinitionName"))) {
            processDefinitionQuery = processDefinitionQuery.processDefinitionNameLike(pd.get("processDefinitionName").toString());//??????????????????name??????
        }
        processDefinitionQuery = processDefinitionQuery.processDefinitionKey(pd.get("key").toString()).processDefinitionVersionLowerThan(version).orderByProcessDefinitionName().desc()//???????????????????????????????????????
                .orderByProcessDefinitionVersion().desc();//???????????????????????????
        int firstResult = (page.getIndex()-1)*page.getShowCount();
        List<ProcessDefinition> processDefinitionList = processDefinitionQuery.listPage(firstResult,page.getShowCount());
        //???????????????
        int total = (int) processDefinitionQuery.count();
        List<Map<String ,Object>> allList = new ArrayList<>();
        for (ProcessDefinition processDefinition : processDefinitionList) {
            PageData p = PageData.getInstance().objToPd(processDefinition);
            p.put("id",processDefinition.getId());
            allList.add(p);
        }

        System.out.println(allList);
        Json json = new Json();
        json.setMsg("?????????????????????");
        json.setData(allList);
        json.setCount(total);
        json.setSuccess(true);
        this.writeJson(response,json);
    }


    //==============================????????????==============================
    /**
     * @Description: submitStartFormAndStartProcessInstance
     * @Param: [map, request, response, session]
     * @return: void
     * @Author: anxingtao
     * @Date: 2020-9-9 11:27
     */
    @GetMapping("/startProcessInstanceByKey")
    @PreAuthorize("hasAnyAuthority('processDefinition:startProcessInstanceByKey')")
    public void submitStartFormAndStartProcessInstance(HttpServletRequest request, HttpServletResponse response,HttpSession session) throws Exception {
        PageData pd = new PageData(request);
        String businessKey = "businessKey";
        //??????????????????
        String userParams = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("------????????????????????????-------");
        System.out.println(userParams);
        Authentication.setAuthenticatedUserId(userParams.split(":")[2]+"_"+userParams.split(":")[1]);
        System.out.println("###:"+pd.get("processDefinitionKey").toString());
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(pd.get("processDefinitionKey").toString(),businessKey,pd);
        Json json = new Json();
        json.setData(PageData.getInstance().objToMap(processInstance));
        json.setMsg("?????????????????????");
        json.setSuccess(true);
        this.writeJson(response,json);
    }



    //===============================????????????=====================================

    /**
     * @Description: verifyIsExistModel ??????????????????????????????
     * @Param: [request, response]
     * @return: void
     * @Author: anxingtao
     * @Date: 2020-11-9 23:24
     */
    @GetMapping("/verifyIsExistModel")
    @PreAuthorize("hasAnyAuthority('processDefinition:verifyIsExistModel')")
    public void verifyIsExistModel(HttpServletRequest request,HttpServletResponse response) throws Exception {
        PageData pd = new PageData(request);
        //????????????id??????????????????
        Json json = new Json();
        Model model = repositoryService.createModelQuery().deploymentId(pd.get("deploymentid").toString()).singleResult();
        if(Verify.verifyIsNotNull(model)){
            json.setMsg("?????????????????????");
            json.setSuccess(true);
        }else{
            json.setMsg("????????????????????????");
            json.setSuccess(false);
        }
        this.writeJson(response,json);
    }



    /**
     * ???????????????
     * @param httpServletResponse response entity
     */
    @GetMapping("/readResource")
    public void readResource(HttpServletRequest request,HttpServletResponse httpServletResponse){
        PageData pd = new PageData(request);
        String procDefinitionKey = pd.get("procDefinitionKey").toString();
        if (org.springframework.util.StringUtils.isEmpty(procDefinitionKey)) {
            System.out.println("procDefinitionKey");
        }
        try {
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(procDefinitionKey).latestVersion().singleResult();
            InputStream img = processService.getFlowImgByProcessDefinitionId(processDefinition.getId());
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
     * @title findDefinitionList
     * @description ????????????????????????
     * @author Administrator
     * @updateTime 2021/9/30 0030 1:00
     */
    @GetMapping("/findDefinitionList")
    public void findDefinitionList(HttpServletRequest request,HttpServletResponse response) throws Exception {
        PageData pd = new PageData(request);
        List<Map<String ,Object>> allList = new ArrayList<>();
        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();//??????????????????????????????
        processDefinitionQuery = processDefinitionQuery.orderByProcessDefinitionName().desc()//???????????????????????????????????????
                .orderByProcessDefinitionVersion().desc().latestVersion();//???????????????????????????
        List<ProcessDefinition> processDefinitionList = processDefinitionQuery.list();
        for (ProcessDefinition processDefinition : processDefinitionList) {
            allList.add(BeanUtil.beanToMap(processDefinition));
        }
        //????????????id??????????????????
        Json json = new Json();
        json.setData(allList);
        json.setMsg("???????????????????????????");
        json.setSuccess(true);
        this.writeJson(response,json);
    }



}
