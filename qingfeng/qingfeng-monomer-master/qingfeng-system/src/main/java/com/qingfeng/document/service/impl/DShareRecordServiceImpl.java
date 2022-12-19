package com.qingfeng.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qingfeng.common.mapper.GraphicMapper;
import com.qingfeng.common.service.IGraphicService;
import com.qingfeng.document.mapper.DShareRecordMapper;
import com.qingfeng.document.service.IDShareRecordService;
import com.qingfeng.entity.QueryRequest;
import com.qingfeng.entity.common.Graphic;
import com.qingfeng.entity.document.DShareRecord;
import com.qingfeng.utils.Verify;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @ProjectName DShareRecordServiceImpl
 * @author Administrator
 * @version 1.0.0
 * @Description DShareRecordServiceImpl
 * @createTime 2022/6/15 0015 23:28
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class DShareRecordServiceImpl extends ServiceImpl<DShareRecordMapper, DShareRecord> implements IDShareRecordService {

    /**
     * @title findListPage
     * @description 查询数据分页列表
     * @author Administrator
     * @updateTime 2022/6/15 0015 23:28
     */
    public IPage<DShareRecord> findListPage(DShareRecord dShareRecord, QueryRequest request){
        Page<DShareRecord> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.baseMapper.findListPage(page, dShareRecord);
    }

}