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
 * @Description 文档分类表
 * @createTime 2022/6/15 0015 22:00
 */
@Data
@TableName("document_dclassify")
public class DClassify implements Serializable {

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
     * 名称
     */
    @TableField("name")
    private String name;
    /**
     * 父级id
     */
    @TableField("parent_id")
    private String parent_id;
    /**
     * 等级
     */
    @TableField("level_num")
    private String level_num;
    /**
     * 级联字段
     */
    @TableField("dc_cascade")
    private String dc_cascade;
    /**
     * 排序
     */
    @TableField("order_by")
    private String order_by;
    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
    /**
     * 创建人
     */
    @TableField("create_user")
    private String create_user;
    /**
     * 创建组织
     */
    @TableField("create_organize")
    private String create_organize;
    /**
     * 创建时间
     */
    @TableField("create_time")
    private String create_time;
    /**
     * 修改人
     */
    @TableField("update_user")
    private String update_user;
    /**
     * 修改时间
     */
    @TableField("update_time")
    private String update_time;


    @TableField(exist = false)
    private String child_num;
    @TableField(exist = false)
    private String parent_name;
    @TableField(exist = false)
    private String user_id;

}
