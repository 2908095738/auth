package com.bbs.stream.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 审批
 */
@Data
@TableName(value = "stream")
public class Stream implements Serializable {

    /**
     * 唯一标识符
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 创建用户id
     */
    @TableField(value = "create_uid")
    private Long createUid;

    /**
     * 审批用户id
     */
    @TableField(value = "leadr")
    private Long leadr;

    /**
     * 1：请假
     * 审批类型
     */
    @TableField(value = "type")
    private Integer type;

    /**
     * 审批内容
     */
    @TableField(value = "content")
    private String content;

    /**
     * 0：已审批
     * 1：待审批
     * 2：被驳回
     * 审批状态
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 租户id
     */
    @TableField(value = "tenant_id")
    private String tenantId;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}