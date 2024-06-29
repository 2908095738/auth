package com.bbs.financial.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 日记账
 * @TableName note
 */
@TableName(value ="note")
@Data
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
    @TableField(value = "revenue")
    private BigDecimal revenue;

    /**
     * 支出
     */
    @TableField(value = "expenses")
    private BigDecimal expenses;

    /**
     * 凭证字
     */
    @TableField(value = "certificate_word")
    private String certificateWord;

    /**
     * 编号（凭证号）
     */
    @TableField(value = "no")
    private Long no;

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
     * 账户id
     */
    @TableField(value = "zh_id")
    private Long zhId;

    /**
     * 公司ID
     */
    @TableField(value = "company_id")
    private Long companyId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}