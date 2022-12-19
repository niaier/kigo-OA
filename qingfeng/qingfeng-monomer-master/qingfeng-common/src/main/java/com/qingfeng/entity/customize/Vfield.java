package com.qingfeng.entity.customize;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
* @title: Vfield
* @projectName: Vfield
* @description: TODO
* @author: qingfeng
* @createTime 2021/4/3 0003 20:30
*/
@Data
@TableName("customize_vfield")
public class Vfield implements Serializable {

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
     * 表id
     */
    @TableField("table_id")
    private String table_id;
    /**
     * 字段名称
     */
    @TableField("field_name")
    private String field_name;
    /**
     * 字段类型
     */
    @TableField("field_type")
    private String field_type;
    /**
     * 字典描述
     */
    @TableField("field_comment")
    private String field_comment;
    /**
     * 字段key
     */
    @TableField("field_key")
    private String field_key;
    /**
     * 动态数据源
     */
    @TableField("dynamic")
    private String dynamic;
    /**
     * 动态数据源Key
     */
    @TableField("dynamicKey")
    private String dynamicKey;
    /**
     * 选项
     */
    @TableField("options")
    private String options;
    /**
     * option_type
     */
    @TableField("option_type")
    private String option_type;

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
     * 创建时间
     */
    @TableField("create_time")
    private String create_time;
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
    private List<String> auth_organize_ids;

    @TableField(exist = false)
    private String auth_user;

    @TableField(exist = false)
    private String start_time;

    @TableField(exist = false)
    private String end_time;

    @TableField(exist = false)
    private String maxLength;

}