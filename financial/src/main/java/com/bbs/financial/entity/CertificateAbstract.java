package com.bbs.financial.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.yulichang.annotation.EntityMapping;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;

/**
 * 记账凭证摘要
 * @TableName certificate_abstract
 */
@TableName(value ="certificate_abstract")
@Data
@NoArgsConstructor
@FieldNameConstants
@AllArgsConstructor
public class CertificateAbstract implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 凭证ID
     */
    @TableField(value = "certificate_id")
    private Long certificateId;

    /**
     * 摘要
     */
    @TableField(value = "certificate_abstract")
    private String certificateAbstract;

    /**
     * 科目 ID
     */
    @TableField(value = "account_id")
    private Long accountId;

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
     * 余额
     */
    @TableField(exist = false)
    private Long surplusMoney;

    /**
     * 权重
     */
    @TableField(value = "weight")
    private Integer weight;

    /**
     * 辅助核算
     */
    @TableField(value = "auxiliary")
    private String auxiliary;

    /**
     * 数量
     */
    @TableField(value = "num")
    private Long num;

    /**
     * 单价
     */
    @TableField(value = "price")
    private Long price;

    /**
     * 币别
     */
    @TableField(value = "currency")
    private String currency;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableField(exist = false)
    private Certificate certificate;

    @TableField(exist = false)
    @EntityMapping(thisField = Fields.accountId, joinField = Account.Fields.id)
    private Account account;

    @TableField(exist = false)
    private AccountAuxiliary accountAuxiliary;
}