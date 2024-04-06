package com.bbs.chat.dto;

import lombok.Data;

import java.util.Date;

/**
 * 评论
 */
@Data
public class CommDto {
    /**
     * 评论用户id
     */
    private Integer commUid;

    /**
     * 评论用户呢称
     */
    private String nickName;

    /**
     * 评论头像路径
     */
    private String avatarPath;

    /**
     * 评论文章/回复评论id
     */
    private Long commId;

    /**
     * 评论类型
     * 1.评论文章
     * 2.回复评论
     */
    private Integer type;

    /**
     * 评论时间
     */
    private Date time;
}