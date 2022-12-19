package com.qingfeng.gencode.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingfeng.entity.gencode.Chuchai;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author qingfeng
 * @title: ChuchaiMapper
 * @projectName qingfeng-cloud
 * @description: TODO
 * @createTime 2021/4/3 0003 20:30
 */
public interface ChuchaiMapper extends BaseMapper<Chuchai> {

    //查询数据分页列表
    IPage<Chuchai> findListPage(Page page, @Param("obj") Chuchai chuchai);

    //查询数据列表
    List<Chuchai> findList(Chuchai chuchai);

}