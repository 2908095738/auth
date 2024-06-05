package com.bbs.financial.entity;

import cn.hutool.core.annotation.Alias;
import cn.hutool.core.annotation.PropIgnore;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bbs.api.auth.User;
import com.bbs.vo.CompanyStructure;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 资产表
 * @TableName asset
 */
@TableName(value ="asset")
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
public class Asset implements Serializable {
    /**
     * 主键
     */
    @PropIgnore
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 公司主键
     */
    @PropIgnore
    @TableField(value = "company_id")
    private Long companyId;

    /**
     * 资产编码
     */
    @Alias(value = "资产编码")
    @TableField(value = "no")
    private String no;

    /**
     * 资产名称
     */
    @Alias(value = "资产名称")
    @TableField(value = "name")
    private String name;

    /**
     * 资产类别
     */
    @TableField(value = "asset_type_id")
    private Long assetTypeId;

    /**
     * 资产类别
     */
    @Alias(value = "资产类别")
    @TableField(exist = false)
    private String assetTypeName;

    /**
     * 部门ID（公司结构ID）
     */
    @PropIgnore
    @TableField(value = "structure_id")
    private Long structureId;

    /**
     * 部门名称
     */
    @Alias(value = "部门")
    @TableField(exist = false)
    private String structureName;

    /**
     * 开始使用日期
     */
    @Alias(value = "开始使用日期")
    @TableField(value = "start_date")
    private Date startDate;

    /**
     * 数量
     */
    @Alias(value = "数量")
    @TableField(value = "num")
    private Long num;

    /**
     * 数量单位
     */
    @PropIgnore
    @TableField(value = "num_unit_id")
    private Long numUnitId;

    /**
     * 数量单位名称
     */
    @Alias(value = "数量单位")
    @TableField(exist = false)
    private String numUnitName;

    /**
     * 规格型号
     */
    @Alias(value = "规格型号")
    @TableField(value = "spec")
    private String spec;

    /**
     * 存放地点
     */
    @Alias(value = "存放地点")
    @TableField(value = "storage_place")
    private String storagePlace;

    /**
     * 使用人id
     */
    @PropIgnore
    @TableField(value = "use_user_id")
    private Long useUserId;

    /**
     * 使用人名称
     */
    @Alias(value = "使用人")
    @TableField(exist = false)
    private String useUserName;

    /**
     * 折旧方法
     */
    @PropIgnore
    @TableField(value = "depreciation_method")
    private Integer depreciationMethod;

    /**
     * 折旧方法
     */
    @Alias(value = "折旧方法")
    @TableField(exist = false)
    private String depreciationMethodName;

    /**
     * 使用月数
     */
    @Alias(value = "使用月数")
    @TableField(value = "durable_months")
    private Integer durableMonths;

    /**
     * 原值
     */
    @Alias(value = "原值")
    @TableField(value = "original_value")
    private Long originalValue;

    /**
     * 税额
     */
    @Alias(value = "税额")
    @TableField(value = "amount_tax_paid")
    private Long amountTaxPaid;

    /**
     * 残值率
     */
    @Alias(value = "残值率")
    @TableField(value = "ratio_remaining")
    private Long ratioRemaining;

    /**
     * 预计残值
     */
    @Alias(value = "预计残值")
    @TableField(value = "ratio_remaining_value")
    private Long ratioRemainingValue;

    /**
     * 减值准备
     */
    @Alias(value = "减值准备")
    @TableField(value = "Impairment")
    private Long impairment;

    /**
     * 已折旧月数
     */
    @Alias(value = "已折旧月数")
    @TableField(value = "depreciation_months")
    private Integer depreciationMonths;

    /**
     * 期初净值=原值-期初累计折旧
     */
    @Alias(value = "期初净值")
    @TableField(value = "begin_period")
    private Long beginPeriod;

    /**
     * 期初累计折旧
     */
    @Alias(value = "期初累计折旧")
    @TableField(value = "begin_depreciation_accumulated")
    private Long beginDepreciationAccumulated;

    /**
     * 平均月折旧额
     */
    @Alias(value = "平均月折旧额")
    @TableField(value = "depreciation_month_value")
    private Long depreciationMonthValue;

    /**
     * 当月折旧额
     */
    @Alias(value = "当月折旧额")
    @TableField(value = "depreciation_now_month_value")
    private Long depreciationNowMonthValue;

    /**
     * 本年折旧额
     */
    @Alias(value = "本年折旧额")
    @TableField(value = "depreciation_year_value")
    private Long depreciationYearValue;

    /**
     * 期末累计折旧
     */
    @Alias(value = "期末累计折旧")
    @TableField(value = "after_depreciation_accumulated")
    private Long afterDepreciationAccumulated;

    /**
     * 期末净值
     */
    @Alias(value = "期末净值")
    @TableField(value = "after_period")
    private Long afterPeriod;

    /**
     * 期末减值准备
     */
    @Alias(value = "期末减值准备")
    @TableField(value = "after_Impairment")
    private Long afterImpairment;

    /**
     * 清理日期
     */
    @Alias(value = "清理日期")
    @TableField(value = "assets_clean_time")
    private Date assetsCleanTime;

    /**
     * 固定资产科目
     */
    @PropIgnore
    @TableField(value = "fixed_assets_account_id")
    private Long fixedAssetsAccountId;

    /**
     * 资产购入对方科目
     */
    @PropIgnore
    @TableField(value = "purchase_assets_other_part_account_id")
    private Long purchaseAssetsOtherPartAccountId;


    /**
     * 税金科目
     */
    @PropIgnore
    @TableField(value = "taxes_account_id")
    private Long taxesAccountId;

    /**
     * 折旧科目
     */
    @PropIgnore
    @TableField(value = "depreciation_account_id")
    private Long depreciationAccountId;

    /**
     * 折旧费用科目
     */
    @PropIgnore
    @TableField(value = "depreciation_cost_account_id")
    private Long depreciationCostAccountId;

    /**
     * 资产清理科目
     */
    @PropIgnore
    @TableField(value = "assets_clean_account_id")
    private Long assetsCleanAccountId;


    /**
     * 减值准备科目
     */
    @PropIgnore
    @TableField(value = "impairment_account_id")
    private Long impairmentAccountId;

    /**
     * 减值准备对方科目
     */
    @PropIgnore
    @TableField(value = "impairment_other_part_account_id")
    private Long impairmentOtherPartAccountId;


    /**
     * 资产凭证id
     */
    @PropIgnore
    @TableField(value = "assets_certificate_id")
    private Long assetsCertificateId;

    /**
     * 资产清理凭证id
     */
    @PropIgnore
    @TableField(value = "assets_clean_certificate_id")
    private Long assetsCleanCertificateId;

    /**
     * 减值凭证id
     */
    @PropIgnore
    @TableField(value = "Impairment_certificate_id")
    private Long impairmentCertificateId;

    /**
     * 其他凭证id
     */
    @PropIgnore
    @TableField(value = "other_certificate_id")
    private Long otherCertificateId;

    @PropIgnore
    @TableField(exist = false)
    private Account fixedAssetsAccount;

    @Alias(value = "固定资产科目")
    @TableField(exist = false)
    private String fixedAssetsAccountName;

    @PropIgnore
    @TableField(exist = false)
    private Account purchaseAssetsOtherPartAccount; // 资产购入对方科目名称或详情

    @TableField(exist = false)
    @Alias(value = "资产购入对方科目")
    private String purchaseAssetsOtherPartAccountName;

    @PropIgnore
    @TableField(exist = false)
    private Account taxesAccount; // 税金科目名称或详情

    @TableField(exist = false)
    @Alias(value = "税金科目")
    private String taxesAccountName;

    @PropIgnore
    @TableField(exist = false)
    private Account depreciationAccount; // 折旧科目名称或详情

    @TableField(exist = false)
    @Alias(value = "折旧科目")
    private String depreciationAccountName;

    @PropIgnore
    @TableField(exist = false)
    private Account depreciationCostAccount; // 折旧费用科目名称或详情

    @TableField(exist = false)
    @Alias(value = "折旧费用科目")
    private String depreciationCostAccountName;

    @PropIgnore
    @TableField(exist = false)
    private Account assetsCleanAccount; // 资产清理科目名称或详情

    @TableField(exist = false)
    @Alias(value = "资产清理科目")
    private String assetsCleanAccountName;

    @PropIgnore
    @TableField(exist = false)
    private Account impairmentAccount; // 减值准备科目名称或详情

    @TableField(exist = false)
    @Alias(value = "减值准备科目")
    private String impairmentAccountName;

    @PropIgnore
    @TableField(exist = false)
    private Account impairmentOtherPartAccount; // 减值准备对方科目名称或详情

    @TableField(exist = false)
    @Alias(value = "减值准备对方科目")
    private String impairmentOtherPartAccountName;

    @PropIgnore
    @TableField(exist = false)
    private Certificate assetsCertificate;

    @TableField(exist = false)
    @Alias(value = "资产凭证")
    private String assetsCertificateName;

    @PropIgnore
    @TableField(exist = false)
    private Certificate assetsCleanCertificate;

    @TableField(exist = false)
    @Alias(value = "资产清理凭证")
    private String assetsCleanCertificateName;

    @PropIgnore
    @TableField(exist = false)
    private Certificate impairmentCertificate; // 减值凭证名称或详情

    @TableField(exist = false)
    @Alias(value = "减值凭证")
    private String impairmentCertificateName;

    @PropIgnore
    @TableField(exist = false)
    private Certificate otherCertificate; // 其他凭证名称或详情

    @TableField(exist = false)
    @Alias(value = "其他凭证")
    private String otherCertificateName;

    /**
     * 状态:正常 清理
     */
    @PropIgnore
    @TableField(value = "status")
    private Integer status;

    /**
     * 状态名称:正常 清理
     */
    @Alias(value = "状态")
    @TableField(exist = false)
    private Integer statusName;

    /**
     * 备注
     */
    @Alias(value = "备注")
    @TableField(value = "remark")
    private String remark;

    /**
     * 创建时间
     */
    @Alias(value = "创建时间")
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 信息创建人
     */
    @PropIgnore
    @TableField(value = "create_by")
    private Long createBy;

    /**
     * 信息创建人名称
     */
    @Alias(value = "信息创建人")
    @TableField(exist = false)
    private String createUserName;

    /**
     * 修改时间
     */
    @Alias(value = "修改时间")
    @TableField(value = "update_time")
    private Date updateTime;

    /**
     * 信息修改人
     */
    @PropIgnore
    @TableField(value = "update_by")
    private Long updateBy;

    /**
     * 信息修改人名称
     */
    @Alias(value = "信息修改人")
    @TableField(exist = false)
    private String updateUserName;

    /**
     * 0表示未删除，1表示已删除
     */
    @PropIgnore
    @TableField(value = "is_deleted")
    private Integer isDeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @PropIgnore
    @TableField(exist = false)
    private AssetNumUnit numUnit;

    @PropIgnore
    @TableField(exist = false)
    private AssetType assetType;

    @PropIgnore
    @TableField(exist = false)
    private CompanyStructure companyStructure;

    @PropIgnore
    @TableField(exist = false)
    private User useUser;

    @PropIgnore
    @TableField(exist = false)
    private User createUser;

    @PropIgnore
    @TableField(exist = false)
    private User updateUser;

    @PropIgnore
    @TableField(exist = false)
    private List<AssetDepreciationCertificate> certificatesDepreciationList;

}