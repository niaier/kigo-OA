package com.qingfeng.gencode.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qingfeng.entity.QueryRequest;
import com.qingfeng.entity.gencode.Chuchai;
import com.qingfeng.gencode.mapper.ChuchaiMapper;
import com.qingfeng.gencode.service.IChuchaiService;
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
 * @title: ChuchaiServiceImpl
 * @projectName qingfeng-cloud
 * @description: TODO
 * @date 2021/3/8 000821:13
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class ChuchaiServiceImpl extends ServiceImpl<ChuchaiMapper, Chuchai> implements IChuchaiService {


    /**
    * @title findListPage
    * @description 查询数据分页列表
    * @author qingfeng
    * @updateTime 2021/4/3 0003 20:57
    */
    public IPage<Chuchai> findListPage(Chuchai chuchai, QueryRequest request){
        Page<Chuchai> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.baseMapper.findListPage(page, chuchai);
    }

    /**
    * @title findListPage
    * @description 查询数据列表
    * @author qingfeng
    * @updateTime 2021/4/3 0003 20:58
    */
    public List<Chuchai> findList(Chuchai chuchai){
        return this.baseMapper.findList(chuchai);
    }

    /**
    * @title saveMycontent
    * @description 保存数据
    * @author qingfeng
    * @updateTime 2021/4/3 0003 20:58
    */
    @Transactional
    public void saveChuchai(Chuchai chuchai){
        // 创建信息
        String id = GuidUtil.getUuid();
        chuchai.setId(id);
        String time = DateTimeUtil.getDateTimeStr();
        chuchai.setCreate_time(time);
        chuchai.setType("1");
        chuchai.setStatus("0");
        //处理数据权限
        String authParams = SecurityContextHolder.getContext().getAuthentication().getName();
        chuchai.setCreate_user(authParams.split(":")[1]);
        chuchai.setCreate_organize(authParams.split(":")[2]);
        this.save(chuchai);

    }

    /**
    * @title updateMycontent
    * @description 更新数据
    * @author qingfeng
    * @updateTime 2021/4/3 0003 20:58
    */
    @Transactional
    public void updateChuchai(Chuchai chuchai){
        // 更新信息
        String id = chuchai.getId();
        String time = DateTimeUtil.getDateTimeStr();
        String authParams = SecurityContextHolder.getContext().getAuthentication().getName();
        chuchai.setUpdate_time(time);
        chuchai.setUpdate_user(authParams.split(":")[1]);
        this.updateById(chuchai);
    }


}