package com.bbs.financial.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.financial.entity.AssetType;
import com.github.yulichang.base.MPJBaseService;

/**
 *
 */
public interface AssetTypeService extends MPJBaseService<AssetType> {

    Page<AssetType> selectJoinPage(Long companyId, Integer current, Integer size);
}
