package ${tablePd.pack_path}.entity.${tablePd.mod_name};

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
* @title: ${tablePd.bus_name?cap_first}
* @projectName: ${tablePd.bus_name?cap_first}
* @description: TODO
* @author: qingfeng
* @createTime 2021/4/3 0003 20:30
*/
@Data
@TableName("${tablePd.table_name}")
public class ${tablePd.bus_name?cap_first} implements Serializable {

    private static final long serialVersionUID = -4352868070794165001L;

<#list fieldList as obj>
<#if obj.field_name == 'id'>
    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.UUID)
    private String id;
</#if>
<#if obj.field_name != 'id'>
    /**
     * ${obj.field_comment}
     */
    @TableField("${obj.field_name}")
    private String ${obj.field_name};
</#if>
<#if obj.show_type == '8'>
    @TableField(exist = false)
    private Collection<UploadFile> ${obj.field_name}FileList;
</#if>
</#list>

<#if tablePd.temp_type == '1'>
    /**
    * 父级id
    */
    @TableField("parent_id")
    private String parent_id;

    @TableField(exist = false)
    private String parent_name;
</#if>

    @TableField(exist = false)
    private List<String> auth_organize_ids;

    @TableField(exist = false)
    private String auth_user;

    @TableField(exist = false)
    private String start_time;

    @TableField(exist = false)
    private String end_time;

    <#if tablePd.temp_type == '0'&&tablePd.open_process == '0'&&tablePd.process_key!=''>
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
    </#if>

}