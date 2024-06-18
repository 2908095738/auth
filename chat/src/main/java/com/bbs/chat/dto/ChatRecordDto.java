package com.bbs.chat.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 聊天记录
 */
@ApiModel("聊天记录")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRecordDto {
    @ApiModelProperty(value = "id", allowableValues = "[1,infinity]", example = "1")
    private Long id;

    /**
     * 发送消息的用户id；-1表示我发给对方
     * TODO -1使用地方待查找
     */
    @ApiModelProperty(value = "发送消息的用户id", allowableValues = "[1,infinity]", example = "1")
    private Long chatUid;

    /**
     * 消息类型
     * 不同消息类型方便前端处理
     * 1.文本消息
     * 2.图片消息
     * 3.视频消息
     */
    @ApiModelProperty(value = "消息类型: 1.文本消息;2.图片消息;3.视频消息", allowableValues = "[1,3]", example = "1")
    private Integer contentType;

    /**
     * 消息内容
     */
    @ApiModelProperty(value = "消息内容", example = "Resp Msg Content")
    private String content;

    /**
     * 消息发送方
     * 1.我发给对方
     * 0.对方发给我
     * TODO 标识符查看哪里使用
     */
    @ApiModelProperty(value = "消息发送方")
    private Integer type;

    //TODO 时间实际返回格式待查找
    @ApiModelProperty(value = "时间")
    private Date time;

    /**
     * 头像地址
     */
    @ApiModelProperty(value = "头像地址", example = "D:\\111.png")
    private String avatar;

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}