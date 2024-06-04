package com.bbs.financial.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 出纳基础金额
 */
@Data
@ApiModel("出纳基础金额")
public class BaseMoneyByCashierDto {
    /**
     * 期初余额
     */
    private BigDecimal oriMoney;

    /**
     * 收入
     */
    private BigDecimal borrowMoney;

    /**
     * 支出
     */
    private BigDecimal loansMoney;

    /**
     * 期末余额
     */
    private BigDecimal endMoney;
}