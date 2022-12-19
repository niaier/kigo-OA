package com.qingfeng.entity.document;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @ProjectName DClassify
 * @author Administrator
 * @version 1.0.0
 * @Description 文档文件管理
 * @createTime 2022/6/15 0015 22:00
 */
@Data
@TableName("document_dfile")
public class DFile implements Serializable {

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
     * 所属分类
     */
    @TableField("classify_id")
    private String classify_id;
    /**
     * file_hash
     */
    @TableField("file_hash")
    private String file_hash;
    /**
     * 文件名称
     */
    @TableField("name")
    private String name;
    /**
     * 文件存储名称
     */
    @TableField("file_name")
    private String file_name;
    /**
     * 文件路径
     */
    @TableField("file_path")
    private String file_path;

    /**
     * 文件类型
     */
    @TableField("file_type")
    private String file_type;
    /**
     * 文件大小
     */
    @TableField("file_size")
    private String file_size;
    /**
     * 文件后缀
     */
    @TableField("file_suffix")
    private String file_suffix;
    /**
     * 是否共享
     */
    @TableField("is_share")
    private String is_share;
    /**
     * 共享分类
     */
    @TableField("share_classify_id")
    private String share_classify_id;
    /**
     * 共享备注
     */
    @TableField("share_remark")
    private String share_remark;
    /**
     * 共享时间
     */
    @TableField("share_time")
    private String share_time;

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
     * 创建时间
     */
    @TableField("create_time")
    private String create_time;
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
    private String user_id;
    @TableField(exist = false)
    private String user_name;

    @TableField(exist = false)
    private String classify_name;

    @TableField(exist = false)
    private String start_time;
    @TableField(exist = false)
    private String end_time;

}
