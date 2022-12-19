package com.qingfeng.activiti.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qingfeng.activiti.mapper.BpmnMapper;
import com.qingfeng.activiti.service.IBpmnService;
import com.qingfeng.entity.QueryRequest;
import com.qingfeng.entity.activiti.Bpmn;
import com.qingfeng.utils.Verify;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @ProjectName BpmnServiceImpl
 * @author Administrator
 * @version 1.0.0
 * @Description bpmn流程设计器
 * @createTime 2021/9/11 0011 18:27
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class BpmnServiceImpl extends ServiceImpl<BpmnMapper, Bpmn> implements IBpmnService {

    /**
     * @title findListPage
     * @description 查询数据分页列表
     * @author Administrator
     * @updateTime 2021/9/11 0011 18:28
     */
    public IPage<Bpmn> findListPage(Bpmn bpmn, QueryRequest request){
        Page<Bpmn> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.baseMapper.findListPage(page, bpmn);
    }


    /**
     * @title findList
     * @description 查询数据列表
     * @author Administrator
     * @updateTime 2021/9/11 0011 18:29
     */
    public List<Bpmn> findList(Bpmn bpmn){
        QueryWrapper queryWrapper = new QueryWrapper();
        if(Verify.verifyIsNotNull(bpmn.getProcessId())){
            queryWrapper.like("process_id",bpmn.getProcessId());
        }
        if(Verify.verifyIsNotNull(bpmn.getProcessName())){
            queryWrapper.like("process_name",bpmn.getProcessName());
        }
        return this.list(queryWrapper);
    }

}