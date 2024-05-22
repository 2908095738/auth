package com.bbs.financial.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @TableName certificate_template_abstract
 */
@TableName(value ="certificate_template_abstract")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificateTemplateAbstract implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 模板ID
     */
    @TableField(value = "template_id")
    private Long templateId;

    /**
     * 摘要
     */
    @TableField(value = "certificate_abstract")
    private String certificateAbstract;

    /**
     * 科目ID
     */
    @TableField(value = "account_id")
    private Long accountId;

    /**
     * 辅助核算
     */
    @TableField(value = "auxiliary")
    private String auxiliary;

    /**
     * 数量
     */
    @TableField(value = "number")
    private Long number;

    /**
     * 币别ID
     */
    @TableField(value = "money_type_id")
    private Long moneyTypeId;

    /**
     * 借方金额
     */
    @TableField(value = "borrow_money")
    private Long borrowMoney;

    /**
     * 贷方金额
     */
    @TableField(value = "loans_money")
    private Long loansMoney;

    /**
     * 权重
     */
    @TableField(value = "weight")
    private Integer weight;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableField(exist = false)
    private MoneyType moneyType;
}