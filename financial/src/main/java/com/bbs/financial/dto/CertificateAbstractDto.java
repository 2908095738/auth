package com.bbs.financial.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.bbs.financial.entity.Account;
import com.bbs.financial.entity.Certificate;
import com.bbs.financial.entity.CertificateAbstract;
import com.github.yulichang.annotation.EntityMapping;
import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * 凭证摘要数据
 */
@Data
@ApiModel("凭证摘要数据")
public class CertificateAbstractDto {
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
    private Long balance;

    /**
     * 权重
     */
    @TableField(value = "weight")
    private Integer weight;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableField(exist = false)
    private Certificate certificate;

    @TableField(exist = false)
    @EntityMapping(thisField = CertificateAbstract.Fields.accountId, joinField = Account.Fields.id)
    private Account account;
}