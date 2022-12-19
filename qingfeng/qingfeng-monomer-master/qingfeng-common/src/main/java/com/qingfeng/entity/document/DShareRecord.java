package com.qingfeng.entity.document;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @ProjectName DClassify
 * @author Administrator
 * @version 1.0.0
 * @Description 共享文件操作记录
 * @createTime 2022/6/15 0015 22:00
 */
@Data
@TableName("document_sharerecord")
public class DShareRecord implements Serializable {

    private static final long serialVersionUID = -4352868070794165001L;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.UUID)
    private String id;
    /**
     * 类型
     */
    @TableField("type")
    private String type;
    /**
     * 文件id
     */
    @TableField("dfile_id")
    private String dfile_id;
    /**
     * 创建人
     */
    @TableField("create_user")
    private String create_user;
    /**
     * 创建时间
     */
    @TableField("create_time")
    private String create_time;

}
