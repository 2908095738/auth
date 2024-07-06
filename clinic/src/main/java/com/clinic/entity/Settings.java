package com.clinic.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 诊所设置
 * @TableName settings
 */
@TableName(value ="settings")
@Data
public class Settings implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 过期预提醒时间（月）
     */
    @TableField(value = "expiry_alert_month")
    private Integer expiryAlertMonth;

    /**
     * 库存统计规则
     */
    @TableField(value = "state_count_rule")
    private Integer stateCountRule;

    /**
     * 统计值（统计方式值，如百分比 10%； 数量）
     */
    @TableField(value = "count_val")
    private Integer countVal;

    /**
     * 统计单位(0最小单位1最大单位)
     */
    @TableField(value = "count_unit")
    private Integer countUnit;

    /**
     * 诊所名称
     */
    @TableField(value = "clinic_name")
    private String clinicName;

    /**
     * 科别：内科，中西医结合，中医
     */
    @TableField(value = "division")
    private String division;

    /**
     * 用户ID
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 创建日期
     */
    @TableField(value = "create_time")
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}