package com.bbs.chat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 消息页上方三个角标
 */
@Data
@TableName(value = "chat_top")
public class ChatTop implements Serializable {

    @TableId(value = "user_id")
    private Long userId;

    /**
     * 点赞、收藏角标
     */
    @TableField(value = "agree_count")
    private Integer agreeCount;

    /**
     * 关注角标
     */
    @TableField(value = "fan_count")
    private Integer fanCount;

    /**
     * 评论角标
     */
    @TableField(value = "comment_count")
    private Integer commentCount;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}