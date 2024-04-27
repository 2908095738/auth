package com.manage.dto.warehouse;


import com.bbs.enums.CommonStatusEnum;
import com.bbs.validation.InEnum;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 管理后台 - ERP 仓库新增/修改
 */
@Data
public class ErpWarehouseSaveReqVO {
    /**
     * 仓库编号
     */
    private Long id;
    /**
     * 仓库名称
     */
    @NotEmpty(message = "仓库名称不能为空")
    private String name;
    /**
     * 仓库地址
     */
    private String address;
    /**
     * 排序
     */
    @NotNull(message = "排序不能为空")
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
     *
     */
    @NotNull(message = "开启状态不能为空")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

}