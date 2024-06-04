package com.bbs.financial.entity;

import cn.hutool.core.annotation.Alias;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 资产关联的科目和凭证表
 * @TableName asset_account_certificate
 */
@TableName(value ="asset_account_certificate")
@Data
public class AssetAccountCertificate implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 资产主键
     */
    @TableField(value = "asset_id")
    private Long assetId;

    /**
     * 清理月份
     */
    @TableField(value = "assets_clean_month")
    private String assetsCleanMonth;

    /**
     * 固定资产科目
     */
    @TableField(value = "fixed_assets_account_id")
    private Long fixedAssetsAccountId;

    /**
     * 资产购入对方科目
     */
    @TableField(value = "purchase_assets_other_part_account_id")
    private Long purchaseAssetsOtherPartAccountId;


    /**
     * 税金科目
     */
    @TableField(value = "taxes_account_id")
    private Long taxesAccountId;

    /**
     * 折旧科目
     */
    @TableField(value = "depreciation_account_id")
    private Long depreciationAccountId;

    /**
     * 折旧费用科目
     */
    @TableField(value = "depreciation_cost_account_id")
    private Long depreciationCostAccountId;

    /**
     * 资产清理科目
     */
    @TableField(value = "assets_clean_account_id")
    private Long assetsCleanAccountId;


    /**
     * 减值准备科目
     */
    @TableField(value = "impairment_account_id")
    private Long impairmentAccountId;

    /**
     * 减值准备对方科目
     */
    @TableField(value = "impairment_other_part_account_id")
    private Long impairmentOtherPartAccountId;


    /**
     * 资产凭证id
     */
    @TableField(value = "assets_certificate_id")
    private Long assetsCertificateId;

    /**
     * 当月折旧凭证id
     */
    @TableField(value = "depreciation_certificate_id")
    private Long depreciationCertificateId;

    /**
     * 资产清理凭证id
     */
    @TableField(value = "assets_clean_certificate_id")
    private Long assetsCleanCertificateId;

    /**
     * 减值凭证id
     */
    @TableField(value = "Impairment_certificate_id")
    private Long impairmentCertificateId;

    /**
     * 其他凭证id
     */
    @TableField(value = "other_certificate_id")
    private Long otherCertificateId;

    @TableField(exist = false)
    private Account fixedAssetsAccount;

    @Alias(value = "固定资产科目")
    @TableField(exist = false)
    private String fixedAssetsAccountName;

    @TableField(exist = false)
    private Account purchaseAssetsOtherPartAccount; // 资产购入对方科目名称或详情

    @TableField(exist = false)
    @Alias(value = "资产购入对方科目")
    private String purchaseAssetsOtherPartAccountName;

    @TableField(exist = false)
    private Account taxesAccount; // 税金科目名称或详情

    @TableField(exist = false)
    @Alias(value = "税金科目")
    private String taxesAccountName;

    @TableField(exist = false)
    private Account depreciationAccount; // 折旧科目名称或详情

    @TableField(exist = false)
    @Alias(value = "折旧科目")
    private String depreciationAccountName;

    @TableField(exist = false)
    private Account depreciationCostAccount; // 折旧费用科目名称或详情

    @TableField(exist = false)
    @Alias(value = "折旧费用科目")
    private String depreciationCostAccountName;

    @TableField(exist = false)
    private Account assetsCleanAccount; // 资产清理科目名称或详情

    @TableField(exist = false)
    @Alias(value = "资产清理科目")
    private String assetsCleanAccountName;

    @TableField(exist = false)
    private Account impairmentAccount; // 减值准备科目名称或详情

    @TableField(exist = false)
    @Alias(value = "减值准备科目")
    private String impairmentAccountName;

    @TableField(exist = false)
    private Account impairmentOtherPartAccount; // 减值准备对方科目名称或详情

    @TableField(exist = false)
    @Alias(value = "减值准备对方科目")
    private String impairmentOtherPartAccountName;

    @TableField(exist = false)
    private Certificate assetsCertificate;

    @TableField(exist = false)
    @Alias(value = "资产凭证")
    private String assetsCertificateName;

    @TableField(exist = false)
    private Certificate depreciationCertificate;

    @TableField(exist = false)
    @Alias(value = "当月折旧凭证")
    private String depreciationCertificateName;

    @TableField(exist = false)
    private Certificate assetsCleanCertificate;

    @TableField(exist = false)
    @Alias(value = "资产清理凭证")
    private String assetsCleanCertificateName;

    @TableField(exist = false)
    private Certificate impairmentCertificate; // 减值凭证名称或详情

    @TableField(exist = false)
    @Alias(value = "减值凭证")
    private String impairmentCertificateName;

    @TableField(exist = false)
    private Certificate otherCertificate; // 其他凭证名称或详情

    @TableField(exist = false)
    @Alias(value = "其他凭证")
    private String otherCertificateName;


    /**
     * 0表示未删除，1表示已删除
     */
    @TableField(value = "is_deleted")
    private Integer isDeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}