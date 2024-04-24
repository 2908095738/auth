package com.bbs.chat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 点赞/收藏
 */
@ApiModel("点赞或收藏")
@Data
public class AgreeDto {

    /**
     * 点赞/收藏id
     * (用户点击查看被点赞、收藏内容)
     */
    @ApiModelProperty(value = "点赞/收藏id", allowableValues = "[1,infinity]", example = "1")
    private Long agreeId;

    /**
     * 点赞/收藏用户id
     */
    @ApiModelProperty(value = "点赞/收藏用户id", allowableValues = "[1,infinity]", example = "1")
    private Long agreeUid;

    /**
     * 点赞/收藏用户名称
     */
    @ApiModelProperty(value = "点赞/收藏用户名称", example = "亚当")
    private String name;

    /**
     * 点赞/收藏用户头像路径
     */
    @ApiModelProperty(value = "点赞/收藏用户头像路径", example = "D:\\111.png")
    private String avatarPath;

    /**
     * 点赞/收藏标识符
     * 1.点赞文章
     * 2.点赞评论
     * 3.收藏文章
     * 优先级不高，可能只是想看看谁进行了点赞、收藏，后续查询具体的被点赞、收藏内容
     */
    @ApiModelProperty(value = "点赞/收藏标识符: 1.点赞文章;2.点赞评论;3.收藏文章", allowableValues = "[1,3]", example = "1")
    private Integer type;

    /**
     * 点赞/收藏时间
     */
    @ApiModelProperty(value = "点赞/收藏时间", example = "2024-06-21")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date time;
}