package com.bbs.financial.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 资产表
 * @TableName asset
 */
@TableName(value ="asset")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Asset implements Serializable {
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
     * 资产类别
     */
    @TableField(value = "asset_type_id")
    private Long assetTypeId;

    /**
     * 部门ID（公司结构ID）
     */
    @TableField(value = "structure_id")
    private Long structureId;

    /**
     * 开始使用日期
     */
    @TableField(value = "start_date")
    private Date startDate;

    /**
     * 数量
     */
    @TableField(value = "num")
    private Long num;

    /**
     * 数量单位
     */
    @TableField(value = "num_unit_id")
    private Long numUnitId;

    /**
     * 规格型号
     */
    @TableField(value = "spec")
    private String spec;

    /**
     * 存放地点
     */
    @TableField(value = "storage_place")
    private String storagePlace;

    /**
     * 使用人id
     */
    @TableField(value = "use_user_id")
    private Long useUserId;

    /**
     * 折旧方法
     */
    @TableField(value = "depreciation_method")
    private Integer depreciationMethod;

    /**
     * 使用月数
     */
    @TableField(value = "durable_months")
    private Integer durableMonths;

    /**
     * 原值
     */
    @TableField(value = "original_value")
    private Long originalValue;

    /**
     * 税额
     */
    @TableField(value = "amount_tax_paid")
    private Long amountTaxPaid;

    /**
     * 残值率
     */
    @TableField(value = "ratio_remaining")
    private Long ratioRemaining;

    /**
     * 预计残值
     */
    @TableField(value = "ratio_remaining_value")
    private Long ratioRemainingValue;

    /**
     * 减值准备
     */
    @TableField(value = "Impairment")
    private Long impairment;

    /**
     * 已折旧月数
     */
    @TableField(value = "depreciation_months")
    private Integer depreciationMonths;

    /**
     * 期初净值=原值-期初累计折旧
     */
    @TableField(value = "begin_period")
    private Long beginPeriod;

    /**
     * 期初累计折旧
     */
    @TableField(value = "begin_depreciation_accumulated")
    private Long beginDepreciationAccumulated;

    /**
     * 平均月折旧额
     */
    @TableField(value = "depreciation_month_value")
    private Long depreciationMonthValue;

    /**
     * 当月折旧额
     */
    @TableField(value = "depreciation_now_month_value")
    private Long depreciationNowMonthValue;

    /**
     * 本年折旧额
     */
    @TableField(value = "depreciation_year_value")
    private Long depreciationYearValue;

    /**
     * 期末累计折旧
     */
    @TableField(value = "after_depreciation_accumulated")
    private Long afterDepreciationAccumulated;

    /**
     * 期末净值
     */
    @TableField(value = "after_period")
    private Long afterPeriod;

    /**
     * 期末减值准备
     */
    @TableField(value = "after_Impairment")
    private Long afterImpairment;

    /**
     * 清理月份
     */
    @TableField(value = "assets_clean_month")
    private String assetsCleanMonth;

    /**
     * 状态:正常 清理
     */
    @TableField(value = "status")
    private Integer status;

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
    private AssetAccountCertificate assetAccountCertificate;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}