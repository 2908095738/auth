package com.bbs.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName system
 */
@TableName(value ="system")
@Data
public class System implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 系统名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 管理员ID
     */
    @TableField(value = "admin")
    private Long admin;

    /**
     * 系统描述
     */
    @TableField(value = "description")
    private String description;

    /**
     * 父级系统 ID（0为最大级别）
     */
    @TableField(value = "pid")
    private Long pid;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 创建人
     */
    @TableField(value = "create_by")
    private Long createBy;

    /**
     * 修改时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    /**
     * 修改人
     */
    @TableField(value = "update_by")
    private Long updateBy;

    /**
     * 系统状态（0正常）
     */
    @TableField(value = "state")
    private Integer state;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}