package com.bbs.financial.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;


/**
 * 资产变动表
 * @TableName asset_change_log
 */
@TableName(value ="asset_change_log")
@Data
public class AssetChangeLog implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 公司主键
     */
    @TableField(value = "company_id")
    private Long companyId;

    /**
     * 录入月份
     */
    @TableField(value = "entry_month")
    private String entryMonth;

    /**
     * 资产编码
     */
    @TableField(value = "no")
    private String no;

    /**
     * 资产名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 变动项
     */
    @TableField(value = "change_item")
    private String changeItem;

    /**
     * 变动前内容
     */
    @TableField(value = "change_befor_value")
    private String changeBeforValue;

    /**
     * 变动后内容
     */
    @TableField(value = "change_after_value")
    private String changeAfterValue;

    /**
     * 变动月份
     */
    @TableField(value = "change_month")
    private String changeMonth;

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
     * 0表示未删除，1表示已删除
     */
    @TableField(value = "is_deleted")
    private Integer isDeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}