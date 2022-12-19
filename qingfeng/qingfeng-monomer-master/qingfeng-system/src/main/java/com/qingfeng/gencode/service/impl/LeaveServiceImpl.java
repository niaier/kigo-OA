package com.qingfeng.gencode.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qingfeng.entity.QueryRequest;
import com.qingfeng.entity.gencode.Leave;
import com.qingfeng.gencode.mapper.LeaveMapper;
import com.qingfeng.gencode.service.ILeaveService;
import com.qingfeng.utils.DateTimeUtil;
import com.qingfeng.utils.GuidUtil;
import com.qingfeng.utils.PageData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author qingfeng
 * @title: LeaveServiceImpl
 * @projectName qingfeng-cloud
 * @description: TODO
 * @date 2021/3/8 000821:13
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class LeaveServiceImpl extends ServiceImpl<LeaveMapper, Leave> implements ILeaveService {


    /**
    * @title findListPage
    * @description 查询数据分页列表
    * @author qingfeng
    * @updateTime 2021/4/3 0003 20:57
    */
    public IPage<Leave> findListPage(Leave leave, QueryRequest request){
        Page<Leave> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.baseMapper.findListPage(page, leave);
    }

    /**
    * @title findListPage
    * @description 查询数据列表
    * @author qingfeng
    * @updateTime 2021/4/3 0003 20:58
    */
    public List<Leave> findList(Leave leave){
        return this.baseMapper.findList(leave);
    }

    /**
    * @title saveMycontent
    * @description 保存数据
    * @author qingfeng
    * @updateTime 2021/4/3 0003 20:58
    */
    @Transactional
    public void saveLeave(Leave leave){
        // 创建信息
        String id = GuidUtil.getUuid();
        leave.setId(id);
        String time = DateTimeUtil.getDateTimeStr();
        leave.setCreate_time(time);
        leave.setType("1");
        leave.setStatus("0");
        //处理数据权限
        String authParams = SecurityContextHolder.getContext().getAuthentication().getName();
        leave.setCreate_user(authParams.split(":")[1]);
        leave.setCreate_organize(authParams.split(":")[2]);
        this.save(leave);

    }

    /**
    * @title updateMycontent
    * @description 更新数据
    * @author qingfeng
    * @updateTime 2021/4/3 0003 20:58
    */
    @Transactional
    public void updateLeave(Leave leave){
        // 更新信息
        String id = leave.getId();
        String time = DateTimeUtil.getDateTimeStr();
        String authParams = SecurityContextHolder.getContext().getAuthentication().getName();
        leave.setUpdate_time(time);
        leave.setUpdate_user(authParams.split(":")[1]);
        this.updateById(leave);
    }


}