package com.qingfeng.customize.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qingfeng.entity.QueryRequest;
import com.qingfeng.entity.customize.Vmenu;
import com.qingfeng.entity.customize.VmenuTfield;
import com.qingfeng.utils.PageData;

import java.util.List;

/**
 * @author qingfeng
 * @title: IVmenuTfieldService
 * @projectName qingfeng-cloud
 * @description: TODO
 * @date 2021/3/8 000821:12
 */
public interface IVmenuTfieldService extends IService<VmenuTfield> {

    //查询字段列表
    List<PageData> findFieldList(PageData pd);

}