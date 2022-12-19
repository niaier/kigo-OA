package com.qingfeng.customize.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingfeng.utils.PageData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @ProjectName VDataMapper
 * @author Administrator
 * @version 1.0.0
 * @Description VDataMapper
 * @createTime 2021/10/1 0001 22:18
 */
public interface VDataMapper extends BaseMapper<PageData> {

    //查询数据分页列表
    IPage<PageData> findListPage(Page page, @Param("obj") PageData pd);

    //查询数据列表
    List<PageData> findList(PageData pd);

    //保存数据
    void saveData(PageData pd);

    //更新数据
    void updateData(PageData pd);

    //查询数据详情
    PageData findInfo(PageData pd);

    //删除子表
    void delChildData(PageData pd);

    //删除主表
    void delData(PageData pd);

    //删除id不存在的子表
    void delMyChildData(PageData pd);

    //更新状态
    void updateStatus(PageData pd);

}