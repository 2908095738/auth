package com.bbs.content.dto.param;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class CreateCommentParam {

    /**
     * 父类id
     */
    private Long parentId;


    /**
     * 评论内容
     */
    @NotNull(message = "评论内容不能为空！")
    private String content;

    /**
     * 内容Id
     */
    @NotNull(message = "内容id不能为空！")
    private Long newId;

    /**
     * IP
     */
    private String ip;

    /**
     * 评论人id
     */
    private Long createId;

    /**
     * 子评论数量
     */
    private Integer commentNum = 0;
}
