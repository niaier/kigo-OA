package com.qingfeng.customize.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qingfeng.customize.service.IVfieldService;
import com.qingfeng.customize.service.IVmenuTfieldService;
import com.qingfeng.entity.QueryRequest;
import com.qingfeng.entity.customize.Vmenu;
import com.qingfeng.customize.mapper.VmenuMapper;
import com.qingfeng.customize.service.IVmenuService;
import com.qingfeng.entity.customize.VmenuTfield;
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
 * @title: VmenuServiceImpl
 * @projectName qingfeng-cloud
 * @description: TODO
 * @date 2021/3/8 000821:13
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class VmenuServiceImpl extends ServiceImpl<VmenuMapper, Vmenu> implements IVmenuService {

    @Autowired
    public IVmenuTfieldService vmenuTfieldService;

    /**
    * @title findListPage
    * @description 查询数据分页列表
    * @author qingfeng
    * @updateTime 2021/4/3 0003 20:57
    */
    public IPage<Vmenu> findListPage(Vmenu vmenu, QueryRequest request){
        Page<Vmenu> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.baseMapper.findListPage(page, vmenu);
    }

    /**
    * @title findListPage
    * @description 查询数据列表
    * @author qingfeng
    * @updateTime 2021/4/3 0003 20:58
    */
    public List<Vmenu> findList(Vmenu vmenu){
        return this.baseMapper.findList(vmenu);
    }

    /**
    * @title saveMycontent
    * @description 保存数据
    * @author qingfeng
    * @updateTime 2021/4/3 0003 20:58
    */
    @Transactional
    public void saveVmenu(Vmenu vmenu){
        // 创建信息
        String id = GuidUtil.getUuid();
        vmenu.setId(id);
        String time = DateTimeUtil.getDateTimeStr();
        vmenu.setCreate_time(time);
        //处理数据权限
        String authParams = SecurityContextHolder.getContext().getAuthentication().getName();
        vmenu.setCreate_user(authParams.split(":")[1]);
        vmenu.setCreate_organize(authParams.split(":")[2]);
        this.save(vmenu);
        //保存关联菜单信息
        List<VmenuTfield> fieldList = vmenu.getFieldList();
        fieldList.forEach((item)->{
            item.setId(GuidUtil.getUuid());
            item.setMenu_id(id);
            item.setCreate_time(time);
            item.setCreate_user(authParams.split(":")[1]);
            item.setCreate_organize(authParams.split(":")[2]);
        });
        this.vmenuTfieldService.saveBatch(fieldList);
    }

    /**
    * @title updateMycontent
    * @description 更新数据
    * @author qingfeng
    * @updateTime 2021/4/3 0003 20:58
    */
    @Transactional
    public void updateVmenu(Vmenu vmenu){
        // 更新信息
        String id = vmenu.getId();
        String time = DateTimeUtil.getDateTimeStr();
        String authParams = SecurityContextHolder.getContext().getAuthentication().getName();
        vmenu.setUpdate_time(time);
        vmenu.setUpdate_user(authParams.split(":")[1]);
        this.updateById(vmenu);

        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("menu_id",id);
        this.vmenuTfieldService.remove(queryWrapper);
        //保存关联菜单信息
        List<VmenuTfield> fieldList = vmenu.getFieldList();
        fieldList.forEach((item)->{
            item.setId(GuidUtil.getUuid());
            item.setMenu_id(id);
            item.setCreate_time(time);
            item.setCreate_user(authParams.split(":")[1]);
            item.setCreate_organize(authParams.split(":")[2]);
        });
        this.vmenuTfieldService.saveBatch(fieldList);
    }


}