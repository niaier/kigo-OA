package com.qingfeng.entity.activiti;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @ProjectName Bpmn
 * @author Administrator
 * @version 1.0.0
 * @Description bpmn流程设计信息
 * @createTime 2021/9/11 0011 18:24
 */
@Data
@TableName("activiti_bpmn")
public class Bpmn implements Serializable {

    private static final long serialVersionUID = -4352868070794165001L;


    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    /**
     * 类型
     */
    @TableField("type")
    private String type;

    /**
     * 流程id
     */
    @TableField("processId")
    private String processId;

    /**
     * 流程名称
     */
    @TableField("processName")
    private String processName;

    /**
     * 版本标签
     */
    @TableField("versionTag")
    private String versionTag;

    /**
     * 状态
     */
    @TableField("isExecutable")
    private String isExecutable;

    /**
     * 流程模型
     */
    @TableField("xmlString")
    private String xmlString;

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

}