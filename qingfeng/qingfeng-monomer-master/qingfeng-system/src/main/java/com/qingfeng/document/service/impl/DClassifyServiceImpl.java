package com.qingfeng.document.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qingfeng.document.mapper.DClassifyMapper;
import com.qingfeng.document.service.IDClassifyService;
import com.qingfeng.entity.QueryRequest;
import com.qingfeng.entity.document.DClassify;
import com.qingfeng.utils.PageData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @ProjectName DClassifyServiceImpl
 * @author Administrator
 * @version 1.0.0
 * @Description DClassifyServiceImpl
 * @createTime 2022/6/15 0015 23:25
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class DClassifyServiceImpl extends ServiceImpl<DClassifyMapper, DClassify> implements IDClassifyService {

    /**
     * @title findListPage
     * @description 查询数据分页列表
     * @author Administrator
     * @updateTime 2022/6/15 0015 23:25
     */
    public IPage<DClassify> findListPage(DClassify dClassify, QueryRequest request){
        Page<DClassify> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.baseMapper.findListPage(page, dClassify);
    }

    //查询数据列表
    public List<DClassify> findList(DClassify dClassify){
        return this.baseMapper.findList(dClassify);
    }


}