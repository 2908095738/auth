package com.bbs.dto.param;

import lombok.Data;

@Data
public class CreateThumbParam {

    /**
     * 话题或评论id
     */
    private Long tcId;

    /**
     * 发布话题或评论的用户id
     */
    private Long postUserId;

    /**
     * 话题或评论点赞的用户id
     */
    private Long userId;

    /**
     * 点赞内容摘要
     */
    private String tcSummary;


}
