package com.bbs.financial.dto;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 日记账表格数据
 */
@Data
@ApiModel("日记账表格数据")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ExcelNoteDto extends BaseMoneyByCashierDto {
    /**
     * 日期
     */
    private String dateStr;

    /**
     * 摘要
     */
    private String certificateAbstract;

    /**
     * 对方账户
     */
    private String heSubjName;

    /**
     * 科目id
     */
    private Long subjId;

    /**
     * 余额
     */
    private BigDecimal lessMoney;

    /**
     * 凭证
     */
    private String cert;

    /**
     * 制单人
     */
    private String makeName;
}