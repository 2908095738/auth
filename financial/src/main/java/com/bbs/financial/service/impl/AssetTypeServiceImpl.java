package com.bbs.financial.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.financial.entity.Account;
import com.bbs.financial.entity.AssetType;
import com.bbs.financial.mapper.AssetTypeMapper;
import com.bbs.financial.service.AssetTypeService;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
public class AssetTypeServiceImpl extends MPJBaseServiceImpl<AssetTypeMapper, AssetType>
    implements AssetTypeService{

    @Override
    public Page<AssetType> selectJoinPage(Long loginSetId, Integer current, Integer size) {
        return selectJoinListPage(new Page<>(current, size), AssetType.class, new MPJLambdaWrapper<AssetType>()
                .selectAll(AssetType.class)
                .selectAssociation("FixedAssets",Account.class,  AssetType::getFixedAssetsAccount)
                .selectAssociation( "Depreciation", Account.class,AssetType::getDepreciationAccount)
                .leftJoin(Account.class, "FixedAssets", Account::getId, AssetType::getFixedAssetsAccountId)
                .leftJoin(Account.class, "Depreciation", Account::getId, AssetType::getDepreciationAccountId)
                .eq(AssetType::getIsDeleted, 0)
                .eq(AssetType::getAccountingSetId, 0)
                .or()
                .eq(AssetType::getAccountingSetId, loginSetId)
        );
    }
}




