package com.qingfeng.gencode.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qingfeng.entity.QueryRequest;
import com.qingfeng.entity.gencode.Xuesheng;
import com.qingfeng.gencode.mapper.XueshengMapper;
import com.qingfeng.gencode.service.IXueshengService;
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
 * @title: XueshengServiceImpl
 * @projectName qingfeng-cloud
 * @description: TODO
 * @date 2021/3/8 000821:13
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class XueshengServiceImpl extends ServiceImpl<XueshengMapper, Xuesheng> implements IXueshengService {


    /**
    * @title findListPage
    * @description 查询数据分页列表
    * @author qingfeng
    * @updateTime 2021/4/3 0003 20:57
    */
    public IPage<Xuesheng> findListPage(Xuesheng xuesheng, QueryRequest request){
        Page<Xuesheng> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.baseMapper.findListPage(page, xuesheng);
    }

    /**
    * @title findListPage
    * @description 查询数据列表
    * @author qingfeng
    * @updateTime 2021/4/3 0003 20:58
    */
    public List<Xuesheng> findList(Xuesheng xuesheng){
        return this.baseMapper.findList(xuesheng);
    }

    /**
    * @title saveMycontent
    * @description 保存数据
    * @author qingfeng
    * @updateTime 2021/4/3 0003 20:58
    */
    @Transactional
    public void saveXuesheng(Xuesheng xuesheng){
        // 创建信息
        String id = GuidUtil.getUuid();
        xuesheng.setId(id);
        String time = DateTimeUtil.getDateTimeStr();
        xuesheng.setCreate_time(time);
        xuesheng.setType("1");
        xuesheng.setStatus("0");
        //处理数据权限
        String authParams = SecurityContextHolder.getContext().getAuthentication().getName();
        xuesheng.setCreate_user(authParams.split(":")[1]);
        xuesheng.setCreate_organize(authParams.split(":")[2]);
        this.save(xuesheng);

    }

    /**
    * @title updateMycontent
    * @description 更新数据
    * @author qingfeng
    * @updateTime 2021/4/3 0003 20:58
    */
    @Transactional
    public void updateXuesheng(Xuesheng xuesheng){
        // 更新信息
        String id = xuesheng.getId();
        String time = DateTimeUtil.getDateTimeStr();
        String authParams = SecurityContextHolder.getContext().getAuthentication().getName();
        xuesheng.setUpdate_time(time);
        xuesheng.setUpdate_user(authParams.split(":")[1]);
        this.updateById(xuesheng);
    }


}