package com.bbs.dto.param;

import lombok.Data;

@Data
public class CreateCommentParam {

    /**
     * 评论内容
     */
    private String content;

    /**
     * 话题Id
     */
    private Long newId;

    /**
     * IP
     */
    private String ip;

    /**
     * 评论人id
     */
    private Long createId;

}
