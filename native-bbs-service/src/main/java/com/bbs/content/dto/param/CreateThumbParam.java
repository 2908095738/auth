package com.bbs.content.dto.param;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class CreateThumbParam {

    /**
     * 话题id
     */
    @NotNull(message = "内容id不能为空！")
    private Long newId;

    /**
     * 评论id
     */
    private Long commentId;


    /**
     * 点赞类型：1文章2文章下的评论
     */
    @NotNull(message = "点赞类型不能为空！")
    private Integer type;

    /**
     * 发布内容用户id
     */
    @NotNull(message = "发布内容用户id不能为空！")
    private Long postUserId;

    /**
     * 点赞用户id
     */
    @NotNull(message = "点赞用户id不能为空！")
    private Long userId;

    /**
     * 点赞内容摘要
     */
    private String tcSummary;


}
