package com.qingfeng.entity.customize;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.qingfeng.entity.common.UploadFile;
import lombok.Data;
import java.util.Collection;
import java.io.Serializable;
import java.util.List;

/**
* @title: VmenuTfield
* @projectName: VmenuTfield
* @description: vue自定义菜单字段模块
* @author: qingfeng
* @createTime 2021/4/3 0003 20:30
*/
@Data
@TableName("customize_vmenu_tfield")
public class VmenuTfield implements Serializable {

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
     * 表id
     */
    @TableField("menu_id")
    private String menu_id;
    /**
     * 字段id
     */
    @TableField("field_id")
    private String field_id;
    /**
     * 字段名称
     */
    @TableField("field_name")
    private String field_name;
    /**
     * 字段描述
     */
    @TableField("field_comment")
    private String field_comment;
    /**
     * 列表显示
     */
    @TableField("field_list")
    private String field_list;
    /**
     * 查询字段
     */
    @TableField("field_query")
    private String field_query;
    /**
     * 查询方式
     */
    @TableField("query_type")
    private String query_type;
    /**
     * 链接字段
     */
    @TableField("field_link")
    private String field_link;
    /**
     * 字段宽度
     */
    @TableField("field_width")
    private String field_width;
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

    @TableField("field_type")
    private String field_type;

    @TableField(exist = false)
    private List<String> auth_organize_ids;

    @TableField(exist = false)
    private String auth_user;

    @TableField(exist = false)
    private String start_time;

    @TableField(exist = false)
    private String end_time;

}