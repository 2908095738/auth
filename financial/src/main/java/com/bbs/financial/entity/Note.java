package com.bbs.financial.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bbs.api.auth.User;
import com.github.yulichang.annotation.EntityMapping;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 日记账
 *
 * @TableName note
 */
@FieldNameConstants
@TableName(value ="note")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Note implements Serializable {
    /**
     *
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 摘要
     */
    @TableField(value = "certificate_abstract")
    private String certificateAbstract;

    /**
     * 账户对方科目id
     */
    @TableField(value = "he_account_id")
    private Long heAccountId;

    /**
     * 收入
     */
    @TableField(value = "borrow_money")
    private BigDecimal borrowMoney;

    /**
     * 支出
     */
    @TableField(value = "loans_money")
    private BigDecimal loansMoney;

    /**
     * 凭证ID
     */
    @TableField(value = "certificate_id")
    private Long certificateId;

    /**
     * 日期
     */
    @TableField(value = "date")
    private Date date;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 信息创建人
     */
    @TableField(value = "create_by")
    private Long createBy;

    /**
     * 修改时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    /**
     * 信息修改人
     */
    @TableField(value = "update_by")
    private Long updateBy;

    /**
     * 日记账类型：1.普通类型;0.初始金额
     */
    @TableField(value = "note_type")
    private Integer noteType;

    /**
     * 账户id
     */
    @TableField(value = "zh_id")
    private Long zhId;

    /**
     * 公司ID
     */
    @TableField(value = "accounting_set_id")
    private Long accountingSetId;

    @TableField(exist = false)
    @EntityMapping(thisField = Fields.heAccountId, joinField = Account.Fields.id)
    private Account heAccount;

    @TableField(exist = false)
    @EntityMapping(thisField = Fields.zhId, joinField = ZhangHu.Fields.id)
    private ZhangHu zhangHu;

    @TableField(exist = false)
    @EntityMapping(thisField = Fields.certificateId, joinField = Certificate.Fields.id)
    private Certificate certificate;

    @TableField(exist = false)
    private User createUser;

    @TableField(exist = false)
    private User updateUser;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}