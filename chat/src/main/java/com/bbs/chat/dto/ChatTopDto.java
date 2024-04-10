package com.bbs.chat.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

/**
 * 消息页顶部三个角标
 */
@Data
public class ChatTopDto {
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
    @TableField(value = "comm_count")
    private Integer commentCount;
}