package com.qingfeng.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qingfeng.common.mapper.GraphicMapper;
import com.qingfeng.common.service.IGraphicService;
import com.qingfeng.entity.QueryRequest;
import com.qingfeng.entity.common.Graphic;
import com.qingfeng.utils.Verify;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @ProjectName RoleServiceImpl
 * @author qingfeng
 * @version 1.0.0
 * @Description TODO
 * @createTime 2021/4/3 0003 21:36
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class GraphicServiceImpl extends ServiceImpl<GraphicMapper, Graphic> implements IGraphicService {

    /**
     * @title findListPage
     * @description 查询数据分页列表
     * @author qingfeng
     * @updateTime 2021/4/3 0003 21:36
     */
    public IPage<Graphic> findListPage(Graphic graphic, QueryRequest request){
        Page<Graphic> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.baseMapper.findListPage(page, graphic);
    }

    /**
     * @title findList
     * @description 查询数据列表
     * @author qingfeng
     * @updateTime 2021/4/3 0003 21:36
     */
    public List<Graphic> findList(Graphic graphic){
        QueryWrapper queryWrapper = new QueryWrapper();
        if(Verify.verifyIsNotNull(graphic.getTitle())){
            queryWrapper.like("title",graphic.getTitle());
        }
        if(Verify.verifyIsNotNull(graphic.getStatus())){
            queryWrapper.eq("status",graphic.getStatus());
        }
        return this.list(queryWrapper);
    }

}