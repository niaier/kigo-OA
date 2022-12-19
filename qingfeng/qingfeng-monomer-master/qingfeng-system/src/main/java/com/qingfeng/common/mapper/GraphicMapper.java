package com.qingfeng.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingfeng.entity.common.Graphic;
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
public interface GraphicMapper extends BaseMapper<Graphic> {

    //查询数据分页列表
    IPage<Graphic> findListPage(Page page, @Param("obj") Graphic graphic);

}