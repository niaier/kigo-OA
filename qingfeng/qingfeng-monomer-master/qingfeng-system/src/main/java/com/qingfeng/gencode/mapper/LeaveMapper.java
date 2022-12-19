package com.qingfeng.gencode.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingfeng.entity.gencode.Leave;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author qingfeng
 * @title: LeaveMapper
 * @projectName qingfeng-cloud
 * @description: TODO
 * @createTime 2021/4/3 0003 20:30
 */
public interface LeaveMapper extends BaseMapper<Leave> {

    //查询数据分页列表
    IPage<Leave> findListPage(Page page, @Param("obj") Leave leave);

    //查询数据列表
    List<Leave> findList(Leave leave);

}