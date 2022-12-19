package com.qingfeng.document.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qingfeng.entity.QueryRequest;
import com.qingfeng.entity.common.Graphic;
import com.qingfeng.entity.document.DClassify;
import com.qingfeng.entity.document.DFile;

import java.util.List;

/**
 * @ProjectName IDFileService
 * @author Administrator
 * @version 1.0.0
 * @Description 文档信息
 * @createTime 2022/6/15 0015 23:22
 */
public interface IDFileService extends IService<DFile> {

    //查询数据分页列表
    IPage<DFile> findListPage(DFile dFile, QueryRequest request);

    //查询数据列表
    List<DFile> findList(DFile dFile);

}