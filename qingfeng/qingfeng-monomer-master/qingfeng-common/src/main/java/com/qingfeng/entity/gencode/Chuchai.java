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
* @title: Chuchai
* @projectName: Chuchai
* @description: TODO
* @author: qingfeng
* @createTime 2021/4/3 0003 20:30
*/
@Data
@TableName("gencode_chuchai")
public class Chuchai implements Serializable {

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
     * 标题
     */
    @TableField("title")
    private String title;
    /**
     * 出差类型
     */
    @TableField("leave_type")
    private String leave_type;
    /**
     * 出差事由
     */
    @TableField("leave_cause")
    private String leave_cause;
    /**
     * 状态
     */
    @TableField("status")
    private String status;
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

}