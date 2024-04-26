package com.bbs.chat.dto.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 创建消息请求参数
 */
@ApiModel("创建消息请求参数")
@Data
public class CreateChatParam {
    /**
     * 消息接收用户id
     */
    @ApiModelProperty(value = "消息接收用户id", required = true, allowableValues = "[1,infinity]", example = "1")
    private Long acceptUid;

    /**
     * 消息类型
     * 1.文本消息
     * 2.图片消息
     * 3.视频消息
     */
    @ApiModelProperty(value = "消息类型: 1.文本消息;2.图片消息;3.视频消息", required = true, allowableValues = "[1,3]", example = "1")
    private Integer contentType;

    /**
     * 消息内容
     * 如果是图片、视频就存入url
     */
    @ApiModelProperty(value = "消息内容(如果是图片、视频就存入url)", required = true,example = "Create Msg Content")
    private String content;
}