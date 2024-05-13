package com.bbs.financial.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 记账凭证摘要
 * @TableName certificate_abstract
 */
@TableName(value ="certificate_abstract")
@Data
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
     * 科目
     */
    @TableField(value = "account")
    private String account;

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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}