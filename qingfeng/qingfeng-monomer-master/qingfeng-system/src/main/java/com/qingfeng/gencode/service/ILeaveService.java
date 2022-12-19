package com.qingfeng.gencode.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qingfeng.entity.QueryRequest;
import com.qingfeng.entity.gencode.Leave;

import java.util.List;

/**
 * @author qingfeng
 * @title: ILeaveService
 * @projectName qingfeng-cloud
 * @description: TODO
 * @date 2021/3/8 000821:12
 */
public interface ILeaveService extends IService<Leave> {

    //查询数据分页列表
    IPage<Leave> findListPage(Leave leave, QueryRequest request);

    //查询数据列表
    List<Leave> findList(Leave leave);

    //保存数据
    void saveLeave(Leave leave);

    //更新数据
    void updateLeave(Leave leave);

}