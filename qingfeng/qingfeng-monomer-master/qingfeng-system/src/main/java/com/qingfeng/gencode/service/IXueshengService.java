package com.qingfeng.gencode.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qingfeng.entity.QueryRequest;
import com.qingfeng.entity.gencode.Xuesheng;

import java.util.List;

/**
 * @author qingfeng
 * @title: IXueshengService
 * @projectName qingfeng-cloud
 * @description: TODO
 * @date 2021/3/8 000821:12
 */
public interface IXueshengService extends IService<Xuesheng> {

    //查询数据分页列表
    IPage<Xuesheng> findListPage(Xuesheng xuesheng, QueryRequest request);

    //查询数据列表
    List<Xuesheng> findList(Xuesheng xuesheng);

    //保存数据
    void saveXuesheng(Xuesheng xuesheng);

    //更新数据
    void updateXuesheng(Xuesheng xuesheng);

}