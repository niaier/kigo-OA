package com.qingfeng.entity.activiti;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @ProjectName Assignment
 * @author Administrator
 * @version 1.0.0
 * @Description 指定流程指定人
 * @createTime 2021/8/29 0029 16:54
 */
@Data
@TableName("activiti_assignment")
public class Assignment implements Serializable {

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
     * 指定模型
     */
    @TableField("assign_mode")
    private String assign_mode;

    /**
     * 指定内容
     */
    @TableField("assign_content")
    private String assign_content;

    /**
     * 模型id
     */
    @TableField("model_id")
    private String model_id;

    /**
     * 节点key
     */
    @TableField("node_key")
    private String node_key;

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

}