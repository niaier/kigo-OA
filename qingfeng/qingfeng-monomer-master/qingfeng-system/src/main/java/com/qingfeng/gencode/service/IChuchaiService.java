package com.qingfeng.gencode.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qingfeng.entity.QueryRequest;
import com.qingfeng.entity.gencode.Chuchai;

import java.util.List;

/**
 * @author qingfeng
 * @title: IChuchaiService
 * @projectName qingfeng-cloud
 * @description: TODO
 * @date 2021/3/8 000821:12
 */
public interface IChuchaiService extends IService<Chuchai> {

    //查询数据分页列表
    IPage<Chuchai> findListPage(Chuchai chuchai, QueryRequest request);

    //查询数据列表
    List<Chuchai> findList(Chuchai chuchai);

    //保存数据
    void saveChuchai(Chuchai chuchai);

    //更新数据
    void updateChuchai(Chuchai chuchai);

}