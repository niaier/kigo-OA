package com.qingfeng.entity.activiti;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.qingfeng.utils.PageData;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author Administrator
 * @version 1.0.0
 * @ProjectName qingfeng-cloud
 * @Description TODO
 * @createTime 2021年09月20日 19:41:00
 */
@Data
public class CheckTask implements Serializable {

    private String id;
    private String assignee;
    private String assignee_name;
    private String businessKey;
    private String executionId;
    private String name;
    private String originalAssignee;
    private String owner;
    private String parentTaskId;
    private String procdef_name;
    private String processDefinitionId;
    private String processInstanceId;
    private String queryVariables;
    private String suspensionState;
    private String taskDefinitionKey;
    private String tenantId;
    private String approve_opinion;
    private List<PageData> nodeData;

}
