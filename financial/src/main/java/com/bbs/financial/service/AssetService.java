package com.bbs.financial.service;

import com.bbs.financial.entity.Asset;
import com.github.yulichang.extension.mapping.base.MPJDeepService;

import java.util.List;

/**
 *
 */
public interface AssetService extends MPJDeepService<Asset> {

    List<Asset> selectNowJoinList();

    List<Asset> selectJoinList(List<Long> assetIds);
}
