package com.qingfeng.gencode.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingfeng.entity.gencode.Xuesheng;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author qingfeng
 * @title: XueshengMapper
 * @projectName qingfeng-cloud
 * @description: TODO
 * @createTime 2021/4/3 0003 20:30
 */
public interface XueshengMapper extends BaseMapper<Xuesheng> {

    //查询数据分页列表
    IPage<Xuesheng> findListPage(Page page, @Param("obj") Xuesheng xuesheng);

    //查询数据列表
    List<Xuesheng> findList(Xuesheng xuesheng);

}