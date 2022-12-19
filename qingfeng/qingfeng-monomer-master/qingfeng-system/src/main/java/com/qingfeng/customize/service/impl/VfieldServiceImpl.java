package com.qingfeng.customize.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qingfeng.entity.QueryRequest;
import com.qingfeng.entity.customize.Vfield;
import com.qingfeng.customize.mapper.VfieldMapper;
import com.qingfeng.customize.service.IVfieldService;
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
 * @title: VfieldServiceImpl
 * @projectName qingfeng-cloud
 * @description: TODO
 * @date 2021/3/8 000821:13
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class VfieldServiceImpl extends ServiceImpl<VfieldMapper, Vfield> implements IVfieldService {

    //添加字段
    public void addField(PageData pd){
        this.baseMapper.addField(pd);
    }
    //删除字段
    public void delField(PageData pd){
        this.baseMapper.delField(pd);
    }
    //更新字段
    public void updateField(PageData pd){
        this.baseMapper.updateField(pd);
    }


}