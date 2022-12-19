package com.qingfeng.customize.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qingfeng.entity.QueryRequest;
import com.qingfeng.entity.customize.Vfield;
import com.qingfeng.utils.PageData;

import java.util.List;

/**
 * @author qingfeng
 * @title: IVfieldService
 * @projectName qingfeng-cloud
 * @description: TODO
 * @date 2021/3/8 000821:12
 */
public interface IVfieldService extends IService<Vfield> {

    //添加字段
    void addField(PageData pd);
    //删除字段
    void delField(PageData pd);
    //更新字段
    void updateField(PageData pd);

}