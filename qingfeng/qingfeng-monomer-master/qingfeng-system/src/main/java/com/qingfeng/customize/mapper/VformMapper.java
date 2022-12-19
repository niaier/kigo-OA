package com.qingfeng.customize.mapper;

import com.baomidou.mybatisplus.annotation.SqlParser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingfeng.entity.customize.Vform;
import com.qingfeng.utils.PageData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author qingfeng
 * @title: VformMapper
 * @projectName qingfeng-cloud
 * @description: TODO
 * @createTime 2021/4/3 0003 20:30
 */
public interface VformMapper extends BaseMapper<Vform> {

    //查询数据分页列表
    IPage<Vform> findListPage(Page page, @Param("obj") Vform vform);

    //查询数据列表
    List<Vform> findList(Vform vform);

    //创建数据表
    @SqlParser(filter = true)
    void createTable(PageData pd);

    //保存数据
    @SqlParser(filter = true)
    void updateTable(PageData pd);

    //删除数据表
    @SqlParser(filter = true)
    void dropTable(PageData pd);

    //更新数据表字段
    @SqlParser(filter = true)
    void updateTableComment(PageData pd);

    /**
     * @title findTableList
     * @description 查询数据表信息
     * @author Administrator
     * @updateTime 2021/9/27 0027 22:38
     */
    List<PageData> findTableList(PageData pd);

    /**
     * @title findColumndList
     * @description 查询字段列表
     * @author Administrator
     * @updateTime 2022/5/2 0002 23:32
     */
    List<PageData> findColumndList(PageData pd);

}