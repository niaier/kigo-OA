package com.qingfeng.document.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingfeng.entity.document.DClassify;
import com.qingfeng.utils.PageData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @ProjectName GraphicMapper
 * @author Administrator
 * @version 1.0.0
 * @Description GraphicMapper
 * @createTime 2022/6/3 0003 22:46
 */
public interface DClassifyMapper extends BaseMapper<DClassify> {

    //查询数据分页列表
    IPage<DClassify> findListPage(Page page, @Param("obj") DClassify dClassify);

    //查询数据列表
    public List<DClassify> findList(DClassify dClassify);

}