package com.qingfeng.customize.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qingfeng.entity.QueryRequest;
import com.qingfeng.entity.customize.Vform;
import com.qingfeng.utils.PageData;

import java.util.List;

/**
 * @author qingfeng
 * @title: IVformService
 * @projectName qingfeng-cloud
 * @description: TODO
 * @date 2021/3/8 000821:12
 */
public interface IVformService extends IService<Vform> {

    //查询数据分页列表
    IPage<Vform> findListPage(Vform vform, QueryRequest request);

    //查询数据列表
    List<Vform> findList(Vform vform);

    //保存数据
    void saveVform(PageData pd) throws Exception;

    //更新数据
    void updateVform(PageData pd) throws Exception;

    /**
     * @title findTableList
     * @description 查询数据表信息
     * @author Administrator
     * @updateTime 2021/9/27 0027 22:38
     */
    List<PageData> findTableList(PageData pd);

    /**
     * @title findColumndList
     * @description 查询字段列表
     * @author Administrator
     * @updateTime 2022/5/2 0002 23:32
     */
    List<PageData> findColumndList(PageData pd);

    //删除数据
    void del(String[] ids);


    //保存数据
    void saveform(PageData pd) throws Exception;

    //更新数据
    void updateform(PageData pd) throws Exception;

}