package com.bbs.financial.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel("发票辅助核算")
public class InvoiceAuxiliaryDto {
    /**
     * 主键
     */
    private Long id;

    /**
     * 编码
     */
    private String code;

    /**
     * 名称
     */
    private String name;

    /**
     * 备注
     */
    private String remark;
}