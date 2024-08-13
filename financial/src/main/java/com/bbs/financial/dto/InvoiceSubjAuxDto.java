package com.bbs.financial.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * 发票辅助核算
 */
@Data
@ApiModel("发票辅助核算")
public class InvoiceSubjAuxDto {
    /**
     * 唯一标识符
     */
    private Long id;

    /**
     * 编号
     */
    private String no;

    /**
     * 名称
     */
    private String name;
}