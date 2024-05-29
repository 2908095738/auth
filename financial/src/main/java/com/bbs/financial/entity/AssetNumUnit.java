package com.bbs.financial.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 资产数量单位
 * @TableName asset_num_unit
 */
@TableName(value ="asset_num_unit")
@Data
public class AssetNumUnit implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 单位名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 公司ID
     */
    @TableField(value = "company_id")
    private Long companyId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}