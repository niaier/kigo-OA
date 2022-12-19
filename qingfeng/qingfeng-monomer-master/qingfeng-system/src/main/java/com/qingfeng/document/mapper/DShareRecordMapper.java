package com.qingfeng.document.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingfeng.entity.document.DShareRecord;
import org.apache.ibatis.annotations.Param;

/**
 * @ProjectName GraphicMapper
 * @author Administrator
 * @version 1.0.0
 * @Description GraphicMapper
 * @createTime 2022/6/3 0003 22:46
 */
public interface DShareRecordMapper extends BaseMapper<DShareRecord> {

    //查询数据分页列表
    IPage<DShareRecord> findListPage(Page page, @Param("obj") DShareRecord dShareRecord);

}