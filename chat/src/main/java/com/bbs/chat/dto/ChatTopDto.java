package com.bbs.chat.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 消息页顶部未读通知
 */
@ApiModel("消息页顶部未读通知")
@Data
public class ChatTopDto {
    /**
     * 点赞、收藏未读数量
     */
    @ApiModelProperty(value = "点赞、收藏未读数量", allowableValues = "[0,infinity]", example = "666")
    private Integer agreeCount;

    /**
     * 关注角标
     * TODO 不在消息页呈现，之后改成消息通知
     */
    @ApiModelProperty(hidden = true)
    private Integer fanCount;

    /**
     * 评论未读数量
     */
    @ApiModelProperty(value = "评论未读数量", allowableValues = "[0,infinity]", example = "666")
    private Integer commentCount;
}