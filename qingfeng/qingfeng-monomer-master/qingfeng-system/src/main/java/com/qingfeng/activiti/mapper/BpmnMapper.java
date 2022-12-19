package com.qingfeng.activiti.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingfeng.entity.activiti.Bpmn;
import org.apache.ibatis.annotations.Param;


/**
 * @ProjectName BpmnMapper
 * @author Administrator
 * @version 1.0.0
 * @Description bpmn流程设计器
 * @createTime 2021/9/11 0011 18:26
 */
public interface BpmnMapper extends BaseMapper<Bpmn> {

    //查询数据分页列表
    IPage<Bpmn> findListPage(Page page, @Param("obj") Bpmn bpmn);


}