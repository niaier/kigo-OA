package com.qingfeng.activiti.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qingfeng.entity.QueryRequest;
import com.qingfeng.entity.activiti.Bpmn;
import java.util.List;

/**
 * @ProjectName IBpmnService
 * @author Administrator
 * @version 1.0.0
 * @Description bpmn流程设计器
 * @createTime 2021/9/11 0011 18:27
 */
public interface IBpmnService extends IService<Bpmn> {

    //查询数据分页列表
    IPage<Bpmn> findListPage(Bpmn bpmn, QueryRequest request);

    //查询数据列表
    List<Bpmn> findList(Bpmn bpmn);

}