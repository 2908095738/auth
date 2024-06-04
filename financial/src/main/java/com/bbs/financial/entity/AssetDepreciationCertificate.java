package com.bbs.financial.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 资产的折旧凭证
 * @TableName asset_depreciation_certificate
 */
@TableName(value ="asset_depreciation_certificate")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssetDepreciationCertificate implements Serializable {
    /**
     * 
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 折旧资产id
     */
    @TableField(value = "asset_id")
    private Long assetId;

    /**
     * 折旧凭证id
     */
    @TableField(value = "depreciation_certificate_id")
    private Long depreciationCertificateId;

    /**
     * 折旧月份
     */
    @TableField(value = "month")
    private Date month;

    @TableField(exist = false)
    private Certificate certificate;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    public AssetDepreciationCertificate(Long assetId, Long depreciationCertificateId, Date month) {
        this.assetId = assetId;
        this.depreciationCertificateId = depreciationCertificateId;
        this.month = month;
    }
}