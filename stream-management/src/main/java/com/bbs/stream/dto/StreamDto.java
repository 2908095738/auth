package com.bbs.stream.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 审批端
 */
@Data
@ApiModel("审批端")
public class StreamDto {

    /**
     * 审批id
     */
    @ApiModelProperty(value = "审批id", allowableValues = "[1,infinity]", example = "1")
    private Long id;

    /**
     * 提交用户id
     */
    @ApiModelProperty(value = "提交用户id", allowableValues = "[1,infinity]", example = "1")
    private Long createUid;

    /**
     * 审批用户id
     */
    @ApiModelProperty(value = "审批用户id", allowableValues = "[1,infinity]", example = "1")
    private Long leadr;

    /**
     * 审批用户名称
     *  TODO 审批方可能比当前用户职位更高，也没有能调用所有用户信息的接口，故需要另说
     */
//    @ApiModelProperty(value = "审批用户名称")
//    private String leadName;

    /**
     * 创建用户头像
     */
    @ApiModelProperty(value = "创建用户头像")
    private String avatar;

    /**
     * 创建用户名称
     */
    @ApiModelProperty(value = "创建用户名称")
    private String name;

    /**
     * 1：请假
     * 审批类型
     */
    @ApiModelProperty(value = "审批类型: 1.请假;", allowableValues = "[1,1]", example = "1")
    private Integer type;

    /**
     * 审批内容
     */
    @ApiModelProperty(value = "审批内容", example = "shen-pi nei-rong")
    private String content;

    /**
     * 0：已审批
     * 1：待审批
     * 2：被驳回
     * 审批状态
     */
    @ApiModelProperty(value = "审批状态: 0.已审批;1.待审批;2.被驳回", allowableValues = "[0,2]", example = "1")
    private Integer status;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}