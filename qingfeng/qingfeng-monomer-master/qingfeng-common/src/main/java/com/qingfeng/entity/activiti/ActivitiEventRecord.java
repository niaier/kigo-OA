package com.qingfeng.entity.activiti;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @ProjectName ActivitiEvent
 * @author Administrator
 * @version 1.0.0
 * @Description 流程事件监听
 * @createTime 2021/10/7 0007 10:25
 */
@Data
@TableName("activiti_eventRecord")
public class ActivitiEventRecord implements Serializable {

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
     * 流程定义id
     */
    @TableField("processDefinitionId")
    private String processDefinitionId;

    /**
     * 流程定义名称
     */
    @TableField("processDefinitionName")
    private String processDefinitionName;

    /**
     * 流程实例id
     */
    @TableField("processInstanceId")
    private String processInstanceId;
    /**
     * 流程任务id
     */
    @TableField("taskId")
    private String taskId;

    /**
     * 流程任务名称
     */
    @TableField("taskName")
    private String taskName;

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

}