package com.bbs.chat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 点赞
 *
 * @TableName thumb
 */
@TableName(value = "thumb")
@Data
public class Thumb implements Serializable {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 话题或评论id
     */
    @TableField(value = "tc_id")
    private Long tcId;

    /**
     * 点赞类型：
     * 1.文章
     * 2.文章下的评论
     */
    @TableField(value = "type")
    private Integer type;

    /**
     * 发布话题或评论的用户id
     */
    @TableField(value = "post_user_id")
    private Long postUserId;

    /**
     * 话题或评论点赞的用户id
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 修改时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    /**
     * 点赞内容摘要
     */
    @TableField(value = "tc_summary")
    private String tcSummary;

    /**
     * 点赞状态
     * 1.点赞
     * 0.取消点赞
     */
    @TableField(value = "status")
    private Integer status;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}