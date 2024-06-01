package com.bbs.financial.service;

import com.bbs.financial.entity.Asset;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 *
 */
public interface AssetService extends IService<Asset> {

    List<Asset> selectNowJoinList();

    List<Asset> selectJoinList(List<Long> assetIds);
}
