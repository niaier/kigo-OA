package com.qingfeng.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qingfeng.common.mapper.GraphicMapper;
import com.qingfeng.common.service.IGraphicService;
import com.qingfeng.document.mapper.DFileMapper;
import com.qingfeng.document.service.IDFileService;
import com.qingfeng.entity.QueryRequest;
import com.qingfeng.entity.common.Graphic;
import com.qingfeng.entity.document.DClassify;
import com.qingfeng.entity.document.DFile;
import com.qingfeng.utils.Verify;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @title DFileServiceImpl
 * @description DFileServiceImpl
 * @author Administrator
 * @updateTime 2022/6/15 0015 23:26
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class DFileServiceImpl extends ServiceImpl<DFileMapper, DFile> implements IDFileService {

    /**
     * @title findListPage
     * @description 查询数据分页列表
     * @author Administrator
     * @updateTime 2022/6/15 0015 23:26
     */
    public IPage<DFile> findListPage(DFile dFile, QueryRequest request){
        Page<DFile> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.baseMapper.findListPage(page, dFile);
    }

    //查询数据列表
    public List<DFile> findList(DFile dFile){
        return this.baseMapper.findList(dFile);
    }


}