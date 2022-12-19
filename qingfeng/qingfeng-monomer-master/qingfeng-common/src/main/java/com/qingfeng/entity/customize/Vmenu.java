package com.qingfeng.entity.customize;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.qingfeng.utils.PageData;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
* @title: Vmenu
* @projectName: Vmenu
* @description: TODO
* @author: qingfeng
* @createTime 2021/4/3 0003 20:30
*/
@Data
@TableName("customize_vmenu")
public class Vmenu implements Serializable {

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
     * 模块名称
     */
    @TableField("name")
    private String name;
    /**
     * 表id
     */
    @TableField("table_id")
    private String table_id;
    /**
     * 条件约束
     */
    @TableField("query_cond")
    private String query_cond;
    /**
     * 表排序
     */
    @TableField("main_order")
    private String main_order;
    /**
     * 是否开启流程
     */
    @TableField("open_process")
    private String open_process;
    /**
     * 流程定义id
     */
    @TableField("process_id")
    private String process_id;
    /**
     * 流程定义名称
     */
    @TableField(exist = false)
    private String process_name;
    /**
     * 状态类型
     */
    @TableField("status_type")
    private String status_type;
    /**
     * 开启时间查询
     */
    @TableField("open_timequery")
    private String open_timequery;

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
    private List<VmenuTfield> fieldList;

    @TableField(exist = false)
    private List<PageData> fieldPdList;

    @TableField(exist = false)
    private String table_comment;
    @TableField(exist = false)
    private Vform vform;
}