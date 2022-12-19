package com.qingfeng.activiti.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qingfeng.activiti.mapper.AssignmentMapper;
import com.qingfeng.activiti.service.IAssignmentService;
import com.qingfeng.entity.activiti.Assignment;
import com.qingfeng.utils.PageData;
import org.activiti.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @title AssignmentServiceImpl
 * @description 流程指定人
 * @author Administrator
 * @updateTime 2021/8/29 0029 16:58
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class AssignmentServiceImpl extends ServiceImpl<AssignmentMapper, Assignment> implements IAssignmentService {

    @Autowired
    private AssignmentMapper assignmentMapper;

    /**
     * @title findActivitiAssignment
     * @description 查询流程节点办理人
     * @author Administrator
     * @updateTime 2021/9/5 0005 16:28
     */
    public PageData findActivitiAssignment(PageData pd){
        return assignmentMapper.findActivitiAssignment(pd);
    }

}