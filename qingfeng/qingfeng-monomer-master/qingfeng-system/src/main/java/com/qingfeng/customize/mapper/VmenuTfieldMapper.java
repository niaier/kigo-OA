package com.qingfeng.customize.mapper;

import com.baomidou.mybatisplus.annotation.SqlParser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingfeng.entity.customize.VmenuTfield;
import com.qingfeng.utils.PageData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author qingfeng
 * @title: VmenuTfieldMapper
 * @projectName qingfeng-cloud
 * @description: TODO
 * @createTime 2021/4/3 0003 20:30
 */
@SqlParser(filter=true)
public interface VmenuTfieldMapper extends BaseMapper<VmenuTfield> {

    /**
     * @title findFieldList
     * @description 查询字段列表
     * @author Administrator
     * @updateTime 2021/9/29 0029 22:12
     */
    List<PageData> findFieldList(PageData pd);

}