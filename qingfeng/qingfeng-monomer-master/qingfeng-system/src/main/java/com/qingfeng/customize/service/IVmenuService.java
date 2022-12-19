package com.qingfeng.customize.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qingfeng.entity.QueryRequest;
import com.qingfeng.entity.customize.Vmenu;

import java.util.List;

/**
 * @author qingfeng
 * @title: IVmenuService
 * @projectName qingfeng-cloud
 * @description: TODO
 * @date 2021/3/8 000821:12
 */
public interface IVmenuService extends IService<Vmenu> {

    //查询数据分页列表
    IPage<Vmenu> findListPage(Vmenu vmenu, QueryRequest request);

    //查询数据列表
    List<Vmenu> findList(Vmenu vmenu);

    //保存数据
    void saveVmenu(Vmenu vmenu);

    //更新数据
    void updateVmenu(Vmenu vmenu);

}