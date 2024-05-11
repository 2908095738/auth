package com.bbs.content.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 内容表
 * @TableName article
 */
@TableName(value ="article")
@Data
public class Article implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 编号
     */
    @TableField(value = "no")
    private String no;

    /**
     * 标题
     */
    @TableField(value = "title")
    private String title;

    /**
     * 轮播图（JSONStr, 含封面）
     */
    @TableField(value = "cover")
    private String cover;

    /**
     * 设定发布时间
     */
    @TableField(value = "scheduled_release_time")
    private Date scheduledReleaseTime;

    /**
     * 精度
     */
    @TableField(value = "longitude")
    private String longitude;

    /**
     * 纬度
     */
    @TableField(value = "latitude")
    private String latitude;

    /**
     * 发布地址
     */
    @TableField(value = "addr")
    private String addr;

    /**
     * 发布时间
     */
    @TableField(value = "release_time")
    private Date releaseTime;

    /**
     * 创建id
     */
    @TableField(value = "create_id")
    private Long createId;

    /**
     * 发表时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 修改id
     */
    @TableField(value = "update_id")
    private Long updateId;

    /**
     * 修改时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}