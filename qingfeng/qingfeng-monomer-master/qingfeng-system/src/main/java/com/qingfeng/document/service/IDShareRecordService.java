package com.qingfeng.document.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qingfeng.entity.QueryRequest;
import com.qingfeng.entity.common.Graphic;
import com.qingfeng.entity.document.DShareRecord;

import java.util.List;

/**
 * @ProjectName IDShareRecordService
 * @author Administrator
 * @version 1.0.0
 * @Description 文档分享记录
 * @createTime 2022/6/15 0015 23:23
 */
public interface IDShareRecordService extends IService<DShareRecord> {

    //查询数据分页列表
    IPage<DShareRecord> findListPage(DShareRecord dShareRecord, QueryRequest request);

}