package com.bbs.chat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    private Long commUid;

    /**
     * 评论用户呢称
     */
    private String name;

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
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date time;
}