package com.bbs.stream.dto.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 创建审批请求参数
 */
@Data
@Schema(name = "CreateStreamParam", description = "创建审批请求参数")
public class CreateStreamParam {
    /**
     * 公司id
     */
    @Schema(name = "companyId", title = "公司id", required = true, example = "1")
    private Long companyId;

    /**
     * 1：请假
     * 审批类型
     */
    @Schema(name = "type", title = "审批类型", description = "1.请假;", required = true, allowableValues = {"1"}, example = "1")
    private Integer type;

    /**
     * 审批内容
     */
    @Schema(name = "content", title = "内容", required = true, example = "shen-pi nei-rong")
    private String content;
}