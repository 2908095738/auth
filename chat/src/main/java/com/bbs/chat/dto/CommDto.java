package com.bbs.chat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 评论
 */
@ApiModel("评论")
@Data
public class CommDto {
    /**
     * 评论用户id
     */
    @ApiModelProperty(value = "评论用户id", allowableValues = "[1,infinity]", example = "1")
    private Long commUid;

    /**
     * 评论用户名称
     */
    @ApiModelProperty(value = "评论用户名称", example = "亚当")
    private String name;

    /**
     * 评论头像路径
     */
    @ApiModelProperty(value = "评论头像路径", example = "D:\\111.png")
    private String avatarPath;

    /**
     * 评论文章/回复评论id
     */
    @ApiModelProperty(value = "评论文章/回复评论id", allowableValues = "[1,infinity]", example = "1")
    private Long commId;

    /**
     * 评论类型
     * 1.评论文章
     * 2.回复评论
     */
    @ApiModelProperty(value = "评论类型: 1.评论文章;2.回复评论", allowableValues = "[1,2]", example = "1")
    private Integer type;

    /**
     * 评论时间
     */
    @ApiModelProperty(value = "评论时间", example = "2024-06-21")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date time;
}