package com.bbs.financial.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 资产类别
 * @TableName asset_type
 */
@TableName(value ="asset_type")
@Data
public class AssetType implements Serializable {
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
     * 类别编码
     */
    @TableField(value = "code")
    private String code;

    /**
     * 资产类别名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 折旧方法
     */
    @TableField(value = "depreciation_method")
    private String depreciationMethod;

    /**
     * 预计使用年限
     */
    @TableField(value = "durable_years")
    private Integer durableYears;

    /**
     * 预计残值率
     */
    @TableField(value = "ratio_remaining")
    private Long ratioRemaining;

    /**
     * 固定资产科目
     */
    @TableField(value = "fixed_assets_account_id")
    private Long fixedAssetsAccountId;

    /**
     * 固定资产科目
     */
    @TableField(exist = false)
    private String fixedAssetsAccountName;


    /**
     * 折旧科目
     */
    @TableField(value = "depreciation_account_id")
    private Long depreciationAccountId;

    /**
     * 折旧科目
     */
    @TableField(exist = false)
    private String depreciationAccountName;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;

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
     * 0表示未删除，1表示已删除
     */
    @TableField(value = "is_deleted")
    private Integer isDeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}