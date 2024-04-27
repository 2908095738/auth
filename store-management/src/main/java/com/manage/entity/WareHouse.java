package com.manage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 
 * @TableName ware_house
 */
@TableName(value ="ware_house")
@Data
@Accessors(chain = true)
public class WareHouse implements Serializable {
    /**
     * 仓库编号
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 仓库名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 仓库地址
     */
    @TableField(value = "address")
    private String address;

    /**
     * 排序
     */
    @TableField(value = "sort")
    private Long sort;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;

    /**
     * 负责人
     */
    @TableField(value = "principal")
    private String principal;

    /**
     * 仓储费，单位：元
     */
    @TableField(value = "warehouse_price")
    private BigDecimal warehousePrice;

    /**
     * 搬运费，单位：元
     */
    @TableField(value = "truckage_price")
    private BigDecimal truckagePrice;

    /**
     * 开启状态
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 是否默认
     */
    @TableField(value = "default_status")
    private Integer defaultStatus;

    /**
     * 仓库类型
     */
    @TableField(value = "type")
    private Integer type;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}