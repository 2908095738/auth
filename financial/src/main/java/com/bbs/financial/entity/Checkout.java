package com.bbs.financial.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * 结账
 *
 * @TableName checkout
 */
@TableName(value = "checkout")
@Data
public class Checkout implements Serializable {
    /**
     * 唯一标识符
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 月份
     */
    @TableField(value = "monthNum")
    private Integer monthNum;

    /**
     * 是否结账：0.正常;1.结账
     */
    @TableField(value = "is_checkout")
    private Integer isCheckout;

    /**
     * 公司ID
     */
    @TableField(value = "company_id")
    private Long companyId;

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

    public void setDate(String msecStr) {
        this.date = new Date(Long.parseLong(msecStr));
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}