package com.bbs.financial.service;

import com.bbs.financial.entity.Asset;
import com.bbs.financial.entity.AssetDepreciationCertificate;
import com.github.yulichang.extension.mapping.base.MPJDeepService;

import java.util.List;

public interface AssetService extends MPJDeepService<Asset> {

    List<Asset> selectJoinList(List<Long> assetIds);

    /**
     * 计算资产【计提折旧】金额（年，注：月份 = 年 / 12）
     * @param asset 资产
     * @param assetDepreciationCertificateList 资产的折旧凭证
     * @return 资产当前年（以使用时间起始，计算一年）的【计提折旧】金额
     */
    Long computeMoney(Asset asset, List<AssetDepreciationCertificate> assetDepreciationCertificateList);
}
