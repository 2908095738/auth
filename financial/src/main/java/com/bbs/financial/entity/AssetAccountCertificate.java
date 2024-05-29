package com.bbs.financial.entity;

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
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 资产主键
     */
    @TableField(value = "asset_id")
    private Long assetId;

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
     * 资产凭证
     */
    @TableField(value = "assets_certificate_id")
    private Long assetsCertificateId;

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
     * 资产清理凭证
     */
    @TableField(value = "assets_clean_certificate_id")
    private Long assetsCleanCertificateId;

    /**
     * 清理月份
     */
    @TableField(value = "assets_clean_month")
    private String assetsCleanMonth;

    /**
     * 减值准备科目
     */
    @TableField(value = "Impairment_account_id")
    private Long impairmentAccountId;

    /**
     * 减值准备对方科目
     */
    @TableField(value = "Impairment_other_part_account_id")
    private Long impairmentOtherPartAccountId;

    /**
     * 减值凭证
     */
    @TableField(value = "Impairment_certificate_id")
    private Long impairmentCertificateId;

    /**
     * 其他凭证
     */
    @TableField(value = "other_certificate_id")
    private Long otherCertificateId;

    /**
     * 0表示未删除，1表示已删除
     */
    @TableField(value = "is_deleted")
    private Integer isDeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}