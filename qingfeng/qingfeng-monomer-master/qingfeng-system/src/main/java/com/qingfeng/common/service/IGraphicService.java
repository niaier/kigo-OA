package com.qingfeng.common.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qingfeng.entity.QueryRequest;
import com.qingfeng.entity.common.Graphic;

import java.util.List;

/**
 * @ProjectName IRoleService
 * @author qingfeng
 * @version 1.0.0
 * @Description TODO
 * @createTime 2021/4/3 0003 21:28
 */
public interface IGraphicService extends IService<Graphic> {

    //查询数据分页列表
    IPage<Graphic> findListPage(Graphic graphic, QueryRequest request);

    //查询数据列表
    List<Graphic> findList(Graphic graphic);

}