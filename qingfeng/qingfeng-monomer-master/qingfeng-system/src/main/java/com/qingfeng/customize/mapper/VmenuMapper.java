package com.qingfeng.customize.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingfeng.entity.customize.Vmenu;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author qingfeng
 * @title: VmenuMapper
 * @projectName qingfeng-cloud
 * @description: TODO
 * @createTime 2021/4/3 0003 20:30
 */
public interface VmenuMapper extends BaseMapper<Vmenu> {

    //查询数据分页列表
    IPage<Vmenu> findListPage(Page page, @Param("obj") Vmenu vmenu);

    //查询数据列表
    List<Vmenu> findList(Vmenu vmenu);

}