package com.bbs.financial.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

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
    @TableField(value = "price_type_id")
    private Long priceTypeId;

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

    /**
     * 取值
     */
    @TableField(value = "use_field")
    private Long useField;

    /**
     * 薪资类型
     */
    @TableField(value = "salary_type")
    private Integer salaryType;

    /**
     * 应用人员范围
     */
    @TableField(value = "use_employee")
    private String useEmployee;

    /**
     * 借贷类型：1借0贷，默认借
     */
    @TableField(value = "borrow_or_loans_type")
    private Integer borrowOrLoansType;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableField(exist = false)
    private PriceType priceType;

    @TableField(exist = false)
    private Account account;
}