package com.qingfeng.entity.gencode;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.qingfeng.entity.common.UploadFile;
import com.qingfeng.utils.PageData;
import lombok.Data;
import java.util.Collection;
import java.io.Serializable;
import java.util.List;

/**
* @title: Leave
* @projectName: Leave
* @description: TODO
* @author: qingfeng
* @createTime 2021/4/3 0003 20:30
*/
@Data
@TableName("gencode_leave")
public class Leave implements Serializable {

    private static final long serialVersionUID = -4352868070794165001L;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.UUID)
    private String id;
    /**
     * type
     */
    @TableField("type")
    private String type;
    /**
     * title
     */
    @TableField("title")
    private String title;
    /**
     * leave_type
     */
    @TableField("leave_type")
    private String leave_type;
    /**
     * leave_cause
     */
    @TableField("leave_cause")
    private String leave_cause;
    /**
     * status
     */
    @TableField("status")
    private String status;
    /**
     * order_by
     */
    @TableField("order_by")
    private String order_by;
    /**
     * remark
     */
    @TableField("remark")
    private String remark;
    /**
     * create_time
     */
    @TableField("create_time")
    private String create_time;
    /**
     * create_user
     */
    @TableField("create_user")
    private String create_user;
    /**
     * create_organize
     */
    @TableField("create_organize")
    private String create_organize;
    /**
     * update_user
     */
    @TableField("update_user")
    private String update_user;
    /**
     * update_time
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

    //办理人
    @TableField(exist = false)
    private String assignee;
    //处理状态
    @TableField(exist = false)
    private String dealStatus;
    //流程状态
    @TableField(exist = false)
    private String processStatus;
    //办理时间
    @TableField(exist = false)
    private String dealTime;

    //提交类型
    @TableField(exist = false)
    private String submitType;

    //流程定义key
    @TableField(exist = false)
    private String process_key;
    //审批意见
    @TableField(exist = false)
    private String approve_opinion;
    //任务id
    @TableField(exist = false)
    private String taskId;
    @TableField(exist = false)
    private List<PageData> nodeData;

    //流程实例id
    @TableField(exist = false)
    private String processInstanceId;
    //流程业务key
    @TableField(exist = false)
    private String businessKey;

//    //节点Ids
//    @TableField(exist = false)
//    private String node_ids;
//    //用户ids
//    @TableField(exist = false)
//    private String user_ids;
//    //办理类型
//    @TableField(exist = false)
//    private String assign_modes;
//    //多实例
//    @TableField(exist = false)
//    private String multiInstances;

}