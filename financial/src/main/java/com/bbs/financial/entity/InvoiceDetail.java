package com.bbs.financial.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

/**
 * 发票明细
 *
 * @TableName invoice_detail
 */
@Data
@TableName(value = "invoice_detail")
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
public class InvoiceDetail implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 发票id
     */
    @TableField(value = "invoice_id")
    private Long invoiceId;

    /**
     * 明细名称(商品/车辆)
     */
    @TableField(value = "name")
    private String name;

    /**
     * 规格型号/车牌号/车辆识别代号
     */
    @TableField(value = "code")
    private String code;

    /**
     * 单位
     */
    @TableField(value = "unit")
    private String unit;

    /**
     * 数量
     */
    @TableField(value = "quantity")
    private Integer quantity;

    /**
     * 单价
     */
    @TableField(value = "price")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal price;

    /**
     * 税率
     */
    @TableField(value = "tax_rates")
    private BigDecimal taxRates;

    /**
     * 不含税金额
     */
    @TableField(value = "non_tax_money")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal nonTaxMoney;

    /**
     * 税额
     */
    @TableField(value = "tax_money")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal taxMoney;

    /**
     * 发票辅助核算id
     */
    @TableField(value = "abst_aux_id")
    private Long abstAuxId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}