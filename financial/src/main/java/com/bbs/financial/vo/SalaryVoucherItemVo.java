package com.bbs.financial.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class SalaryVoucherItemVo {

    /**
     *
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联的公司凭证ID
     */
    @TableField(value = "company_id")
    private Long companyId;

    /**
     * 核算项目的类型ID
     */
    @TableField(value = "accounting_item_type_id")
    private Long accountingItemTypeId;

    /**
     * 是否启用该项目
     */
    @TableField(value = "is_active")
    private Boolean isActive;

    @TableField(value = "type")
    private Integer type;

    /**
     *核算项目类型的id
     */
    @TableId(value = "id")
    private Long typeId;

    /**
     * 核算项目类型的名称
     */
    @TableField(value = "type_name")
    private String typeName;


}
