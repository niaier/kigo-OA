package com.qingfeng.entity.customize;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
* @title: Vform
* @projectName: Vform
* @description: TODO
* @author: qingfeng
* @createTime 2021/4/3 0003 20:30
*/
@Data
@TableName("customize_vform")
public class Vform implements Serializable {

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
     * 表单类型
     */
    @TableField("table_type")
    private String table_type;
    /**
     * 主表id
     */
    @TableField("main_id")
    private String main_id;
    /**
     * 所属分类
     */
    @TableField("classify")
    private String classify;
    /**
     * 表单名称
     */
    @TableField("table_comment")
    private String table_comment;
    /**
     * 数据表名称
     */
    @TableField("table_name")
    private String table_name;
    /**
     * 表内容
     */
    @TableField("table_content")
    private String table_content;
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
    private List<Vfield> vfields;

}