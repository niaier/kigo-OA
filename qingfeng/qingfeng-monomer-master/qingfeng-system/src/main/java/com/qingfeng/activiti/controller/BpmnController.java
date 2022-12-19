package com.qingfeng.activiti.controller;

import cn.hutool.core.util.XmlUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.qingfeng.activiti.service.IBpmnService;
import com.qingfeng.base.controller.BaseController;
import com.qingfeng.entity.MyResponse;
import com.qingfeng.entity.QueryRequest;
import com.qingfeng.entity.activiti.Bpmn;
import com.qingfeng.exception.MyException;
import com.qingfeng.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.util.*;

/**
 * @ProjectName BpmnController
 * @author Administrator
 * @version 1.0.0
 * @Description bpmn流程设计器
 * @createTime 2021/9/11 0011 18:30
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/activiti/bpmn")
public class BpmnController extends BaseController {

    @Autowired
    private IBpmnService bpmnService;
    @Autowired
    private RepositoryService repositoryService;

    /**
     * @title findListPage
     * @description 查询数据分页列表
     * @author Administrator
     * @updateTime 2021/9/11 0011 18:31
     */
    @GetMapping("/findListPage")
    @PreAuthorize("hasAnyAuthority('bpmn:info')")
    public MyResponse findListPage(QueryRequest queryRequest, Bpmn bpmn) {
        String userParams = SecurityContextHolder.getContext().getAuthentication().getName();
        Map<String, Object> dataTable = MyUtil.getDataTable(bpmnService.findListPage(bpmn, queryRequest));
        return new MyResponse().data(dataTable);
    }

    /**
     * @title save
     * @description 保存方法
     * @author Administrator
     * @updateTime 2021/9/11 0011 18:37
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('bpmn:add')")
    public void save(@Valid @RequestBody Bpmn bpmn,HttpServletResponse response) throws Exception {
        Json json = new Json();
        try {
            //判断processId是否存在，存在则更新，不存在则保存
            Bpmn bm = this.bpmnService.getById(bpmn.getId());
            if (Verify.verifyIsNotNull(bm)){
                // 更新组织信息
                String time = DateTimeUtil.getDateTimeStr();
                String authParams = SecurityContextHolder.getContext().getAuthentication().getName();
                bpmn.setUpdate_time(time);
                bpmn.setUpdate_user(authParams.split(":")[1]);
                this.bpmnService.updateById(bpmn);
            }else{
                String time = DateTimeUtil.getDateTimeStr();
                bpmn.setCreate_time(time);
                bpmn.setType("1");
                //创建用户组织信息
                String authParams = SecurityContextHolder.getContext().getAuthentication().getName();
                bpmn.setCreate_user(authParams.split(":")[1]);
                bpmn.setCreate_organize(authParams.split(":")[2]);
                this.bpmnService.save(bpmn);
            }
            if(bpmn.getType().equals("1")){
                String xmlByte = bpmn.getXmlString();
                String processName =  bpmn.getProcessId()+".bpmn20.xml";
                repositoryService.createDeployment()
                        .name("deploy"+GuidUtil.getCodeNum())
                        .addString(processName, XmlFormatter.format(xmlByte))
                        .deploy();

            }
            json.setSuccess(true);
            json.setMsg("操作成功");
        } catch (Exception e) {
            String message = "操作失败";
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
     * @updateTime 2021/9/11 0011 18:40
     */
    @DeleteMapping("/{ids}")
    @PreAuthorize("hasAnyAuthority('bpmn:del')")
    public void delete(@NotBlank(message = "{required}") @PathVariable String ids,HttpServletResponse response) throws Exception {
        Json json = new Json();
        try {
            String[] del_ids = ids.split(StringPool.COMMA);
            this.bpmnService.removeByIds(Arrays.asList(del_ids));
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
     * @title publish
     * @description 流程发布
     * @author Administrator
     * @updateTime 2022/5/22 0022 21:30
     */
    @GetMapping("/publish")
    @PreAuthorize("hasAnyAuthority('bpmn:publish')")
    public void publish(HttpServletRequest request,HttpServletResponse response) throws IOException {
        PageData pd = new PageData(request);
        System.out.println(pd.toString());
        Bpmn bpmn = this.bpmnService.getById(pd.get("id").toString());
        String xmlByte = bpmn.getXmlString();
        String processName =  bpmn.getProcessId()+".bpmn20.xml";
        repositoryService.createDeployment()
                .name("deploy"+GuidUtil.getCodeNum())
                .addString(processName, XmlFormatter.format(xmlByte))
                .deploy();
        Json json = new Json();
        json.setSuccess(true);

        this.writeJson(response,json);
    }

}
