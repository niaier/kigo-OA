package com.qingfeng.customize.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qingfeng.entity.QueryRequest;
import com.qingfeng.utils.PageData;

import java.util.List;

/**
 * @ProjectName IVDataService
 * @author Administrator
 * @version 1.0.0
 * @Description IVDataService
 * @createTime 2021/10/1 0001 22:20
 */
public interface IVDataService extends IService<PageData> {

    //查询数据分页列表
    IPage<PageData> findListPage(PageData pd, QueryRequest request);

    //查询数据列表
    List<PageData> findList(PageData pd);

    //保存数据
    void saveVData(PageData pd);

    //更新数据
    void updateVData(PageData pd);

    //查询数据详情
    PageData findInfo(PageData pd);

    //删除数据
    void delData(PageData pd);

    //更新状态
    void updateStatus(PageData pd);

    //流程退回
    void rejectAnyNod(PageData pd);

    //流程退回
    void delegateTask(PageData pd);


}