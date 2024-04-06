package com.bbs.chat.dto;

import lombok.Data;

import java.util.Date;

/**
 * 点赞
 */
@Data
public class ThumbDto {
    /**
     * 点赞用户id
     */
    private Long thumbUid;

    private Long uid;

    /**
     * 点赞类型
     * 1.点赞评论
     * 2.点赞文章
     */
    private Integer type;

    private Date time;
}