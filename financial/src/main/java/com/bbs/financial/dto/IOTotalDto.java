package com.bbs.financial.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * 收支汇总表
 */
@Data
@ApiModel("收支汇总表")
public class IOTotalDto extends BaseMoneyByCashierDto {

    /**
     * 账户id
     */
    private Long zhId;

    /**
     * 账户编码
     */
    private String zhCode;

    /**
     * 账户名称
     */
    private String zhName;

    /**
     * 币别名称
     */
    private String mTypeName;
}