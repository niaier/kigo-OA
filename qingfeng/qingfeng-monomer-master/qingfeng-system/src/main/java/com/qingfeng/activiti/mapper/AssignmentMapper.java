package com.qingfeng.activiti.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qingfeng.entity.activiti.Assignment;
import com.qingfeng.utils.PageData;

/**
 * @ProjectName AssignmentMapper
 * @author Administrator
 * @version 1.0.0
 * @Description 流程指定人
 * @createTime 2021/8/29 0029 16:57
 */
public interface AssignmentMapper extends BaseMapper<Assignment> {

    /**
     * @title findActivitiAssignment
     * @description 查询流程节点办理人
     * @author Administrator
     * @updateTime 2021/9/5 0005 16:28
     */
    public PageData findActivitiAssignment(PageData pd);

}