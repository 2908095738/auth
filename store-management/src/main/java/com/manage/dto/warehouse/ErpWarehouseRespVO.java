package com.manage.dto.warehouse;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 管理后台 - ERP 仓库 Response VO
 */
@Data

@Accessors(chain = true)
public class ErpWarehouseRespVO {
    /**
     * 仓库编号
     */
    private Long id;
    /**
     * 仓库名称
     */
    private String name;
    /**
     * 仓库地址
     */
    private String address;
    /**
     * 排序
     */
    private Long sort;
    /**
     * 备注
     */
    private String remark;
    /**
     * 负责人
     */
    private String principal;
    /**
     * 仓储费，单位：元
     */
    private BigDecimal warehousePrice;
    /**
     * 搬运费，单位：元
     */
    private BigDecimal truckagePrice;
    /**
     * 开启状态
     */
    private Integer status;
    /**
     * 是否默认
     */
    private Integer defaultStatus;
    /**
     * 创建时间
     */
    private Date createTime;

}