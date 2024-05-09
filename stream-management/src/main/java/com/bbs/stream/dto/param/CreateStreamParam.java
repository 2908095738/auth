package com.bbs.stream.dto.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 创建审批请求参数
 */
@Data
@ApiModel("创建审批请求参数")
public class CreateStreamParam {
    /**
     * 公司id
     */
    @ApiModelProperty(value = "公司id", required = true, allowableValues = "[1,infinity]", example = "1")
    private Long companyId;

    /**
     * 1：请假
     * 审批类型
     */
    @ApiModelProperty(value = "审批类型", notes = "1.请假;", required = true, allowableValues = "[1,1]", example = "1")
    private Integer type;

    /**
     * 审批内容
     */
    @ApiModelProperty(value = "内容", required = true, example = "shen-pi nei-rong")
    private String content;
}