package com.bbs.financial.service.impl;

import com.bbs.financial.entity.Asset;
import com.bbs.financial.mapper.AssetMapper;
import com.bbs.financial.service.AssetService;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 *
 */
@Service
public class AssetServiceImpl extends MPJBaseServiceImpl<AssetMapper, Asset>
    implements AssetService{


    /**
     * 1开始使用日期月份比当前月份小
     * 2排除折旧方法为不计提折旧的资产
     * @return
     */
    @Override
    public List<Asset> selectNowJoinList() {
        return selectJoinList(Asset.class, new MPJLambdaWrapper<Asset>()
                .selectAll(Asset.class)
                .eq(Asset::getIsDeleted, 0)
                .ne(Asset::getDepreciationMethod, 3)//排除折旧方法为不计提折旧的资产
                .lt(Asset::getStartDate,new Date())//开始使用日期月份比当前月份小
        );
    }

    @Override
    public List<Asset> selectJoinList(List<Long> assetIds) {
        return selectJoinList(Asset.class, new MPJLambdaWrapper<Asset>()
                .selectAll(Asset.class)
                .eq(Asset::getIsDeleted, 0)
                .in(Asset::getId, assetIds)
        );
    }



}




