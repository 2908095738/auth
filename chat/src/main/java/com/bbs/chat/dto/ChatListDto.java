package com.bbs.chat.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 最新消息实体
 */
@ApiModel("最新消息")
@Data
public class ChatListDto {
    private Page<ChatLastDto> lastList;

//    @TableField(exist = false)
//    private Page<ChatDto> chatList;

    /**
     * 未读消息
     *  TODO 暂时当作最新消息使用
     *      -
     *      ChatDto暂时不用，未来搭配实现ChatController.getChat()的已读列表逻辑
     */
    @Data
    public static class ChatLastDto {
        @ApiModelProperty(value = "id", allowableValues = "[1,infinity]", example = "1")
        private Long id;

        /**
         * 发送方用户id
         */
        @ApiModelProperty(value = "发送方用户id", allowableValues = "[0,infinity]", example = "1")
        private Long sendUid;

        /**
         * 发送方用户名称
         */
        @ApiModelProperty(value = "发送方用户名称", example = "燕双鹰")
        private String sendName;

        /**
         * 发送方头像路径
         */
        @ApiModelProperty(value = "发送方头像路径", example = "D:\\111.png")
        private String avatarPath;

        /**
         * 未读消息数量
         * 0：已读最新消息 >0：未读数条最新消息
         */
        @ApiModelProperty(value = "未读消息数量;0.已读最新消息;>0：未读数条最新消息", allowableValues = "[0,infinity]", example = "666")
        private Integer count;

        /**
         * 最新消息类型
         * 如果是图片、视频以文字提示点击进入聊天记录查看
         * 1.文本消息
         * 2.图片消息
         * 3.视频消息
         */
        @ApiModelProperty(value = "最新消息类型;如果是图片、视频以文字提示点击进入聊天记录查看;1.文本消息;2.图片消息;3.视频消息", allowableValues = "[1,3]", example = "1")
        private Integer contentType;

        /**
         * 最新消息
         */
        @ApiModelProperty(value = "最新消息", example = "New Msg")
        private String contentLast;

        /**
         * 最新消息时间
         */
        @ApiModelProperty(value = "最新消息时间")
        private Date timeLast;

        //TODO 查找使用等
        @ApiModelProperty(value = "timeLastStr")
        private String timeLastStr;
    }

    @Data
    public static class ChatDto {
        /**
         * 发送方用户id
         */
        @TableField(value = "send_uid")
        private Long sendUid;

        /**
         * 发送方头像路径
         */
        @TableField(value = "avatar_path")
        private String avatarPath;

        /**
         * 发送方用户名称
         */
        @TableField(value = "send_name")
        private String sendName;

        /**
         * 最新消息类型
         * 如果是图片、视频以文字提示点击进入聊天记录查看
         * 1.文本消息
         * 2.图片消息
         * 3.视频消息
         */
        @TableField(value = "content_type")
        private Integer contentType;

        /**
         * 最新消息
         */
        @TableField(value = "content_last")
        private String contentLast;

        /**
         * 最新消息时间
         */
        @TableField(value = "time_last")
        private Date timeLast;
    }
}