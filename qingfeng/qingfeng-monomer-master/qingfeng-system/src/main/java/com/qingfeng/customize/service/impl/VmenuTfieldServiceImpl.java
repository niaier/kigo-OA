package com.qingfeng.customize.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qingfeng.entity.QueryRequest;
import com.qingfeng.entity.customize.VmenuTfield;
import com.qingfeng.customize.mapper.VmenuTfieldMapper;
import com.qingfeng.customize.service.IVmenuTfieldService;
import com.qingfeng.utils.DateTimeUtil;
import com.qingfeng.utils.GuidUtil;
import com.qingfeng.utils.PageData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author qingfeng
 * @title: VmenuTfieldServiceImpl
 * @projectName qingfeng-cloud
 * @description: TODO
 * @date 2021/3/8 000821:13
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class VmenuTfieldServiceImpl extends ServiceImpl<VmenuTfieldMapper, VmenuTfield> implements IVmenuTfieldService {

    /**
     * @title findFieldList
     * @description 查询字段列表
     * @author Administrator
     * @updateTime 2021/9/29 0029 22:11
     */
    public List<PageData> findFieldList(PageData pd){
        return this.baseMapper.findFieldList(pd);
    }

}