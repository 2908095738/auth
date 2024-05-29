package com.bbs.financial.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.financial.entity.AssetType;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 *
 */
public interface AssetTypeService extends IService<AssetType> {

    Page<AssetType> selectJoinPage(Long companyId, Integer current, Integer size);
}
