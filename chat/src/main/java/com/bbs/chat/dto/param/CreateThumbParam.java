package com.bbs.chat.dto.param;

import lombok.Data;

import java.util.Date;

@Data
public class CreateThumbParam {

    /**
     * 话题或评论id
     */
    private Long tcId;

    /**
     * 点赞类型：1文章2文章下的评论
     */
    private Integer type;

    /**
     * 发布话题或评论的用户id
     */
    private Long postUserId;

    /**
     * 话题或评论点赞的用户id
     */
    private Long userId;

    private Date createTime;

    private Date updateTime;

    private String tcSummary;
}