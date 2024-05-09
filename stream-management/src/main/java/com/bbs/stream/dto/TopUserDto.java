package com.bbs.stream.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 上级用户
 */
@Data
@ApiModel("上级用户")
public class TopUserDto {
    /**
     * 上级用户id
     */
    @ApiModelProperty(value = "上级用户id", allowableValues = "[1,infinity]", example = "1")
    private Long topUid;

    /**
     * 上级用户名称
     */
    @ApiModelProperty(value = "上级用户名称")
    private String name;
}