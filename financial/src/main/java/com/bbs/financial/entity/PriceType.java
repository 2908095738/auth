package com.bbs.financial.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 币别
 * @TableName price_type
 */
@TableName(value ="price_type")
@Data
public class PriceType implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 币别编码
     */
    @TableField(value = "code")
    private String code;

    /**
     * 币别名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 汇率
     */
    @TableField(value = "exchange_rate")
    private String exchangeRate;

    /**
     * 公司ID
     */
    @TableField(value = "company_id")
    private Long companyId;

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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}