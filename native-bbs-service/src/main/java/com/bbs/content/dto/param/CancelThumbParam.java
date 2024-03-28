package com.bbs.content.dto.param;

import lombok.Data;

@Data
public class CancelThumbParam {

    /**
     * 话题id
     */
    private Long newId;

    /**
     * 评论id
     */
    private Long commentId;

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


}
