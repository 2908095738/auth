package com.bbs.financial.service;

import com.bbs.financial.entity.Asset;
import com.github.yulichang.base.MPJBaseService;

import java.util.List;

/**
 *
 */
public interface AssetService extends MPJBaseService<Asset> {

    List<Asset> selectNowJoinList();
}
