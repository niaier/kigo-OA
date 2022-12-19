package com.qingfeng.document.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qingfeng.entity.QueryRequest;
import com.qingfeng.entity.common.Graphic;
import com.qingfeng.entity.document.DClassify;
import com.qingfeng.utils.PageData;

import java.util.List;

/**
 * @ProjectName IDClassifyService
 * @author Administrator
 * @version 1.0.0
 * @Description 文档分类
 * @createTime 2022/6/15 0015 23:22
 */
public interface IDClassifyService extends IService<DClassify> {

    //查询数据分页列表
    IPage<DClassify> findListPage(DClassify dClassify, QueryRequest request);

    //查询数据列表
    List<DClassify> findList(DClassify dClassify);

}