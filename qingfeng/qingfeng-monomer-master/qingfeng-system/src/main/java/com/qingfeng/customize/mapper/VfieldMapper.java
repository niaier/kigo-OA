package com.qingfeng.customize.mapper;

import com.baomidou.mybatisplus.annotation.SqlParser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingfeng.entity.customize.Vfield;
import com.qingfeng.utils.PageData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author qingfeng
 * @title: VfieldMapper
 * @projectName qingfeng-cloud
 * @description: TODO
 * @createTime 2021/4/3 0003 20:30
 */
public interface VfieldMapper extends BaseMapper<Vfield> {

    //添加字段
    @SqlParser(filter = true)
    void addField(PageData pd);
    //删除字段
    @SqlParser(filter = true)
    void delField(PageData pd);
    //更新字段
    @SqlParser(filter = true)
    void updateField(PageData pd);

}