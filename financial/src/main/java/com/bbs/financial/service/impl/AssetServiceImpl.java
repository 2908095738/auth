package com.bbs.financial.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.bbs.financial.entity.Asset;
import com.bbs.financial.entity.AssetDepreciationCertificate;
import com.bbs.financial.mapper.AssetMapper;
import com.bbs.financial.service.AssetService;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 *
 */
@Service
public class AssetServiceImpl extends MPJBaseServiceImpl<AssetMapper, Asset>
    implements AssetService{

    @Override
    public List<Asset> selectJoinList(List<Long> assetIds) {
        return selectJoinList(Asset.class, new MPJLambdaWrapper<Asset>()
                .selectAll(Asset.class)
                .eq(Asset::getIsDeleted, 0)
                .in(Asset::getId, assetIds)
        );
    }

    @Override
    public Long computeMoney(Asset asset, List<AssetDepreciationCertificate> assetDepreciationCertificateList) {
        Long yearMoney = 0L;
        if(CollUtil.isEmpty(assetDepreciationCertificateList)){
            if(asset.getDepreciationMethod() == 0){
                //平均年限法
                yearMoney = asset.getDepreciationMonthValue();//平均月折旧额
            }else if(asset.getDepreciationMethod() == 1){
                Long originalValue = asset.getOriginalValue();//原值 10000
                Integer durableMonths = asset.getDurableMonths();//使用月数 60
                if(Objects.isNull(asset.getOriginalValue())||Objects.isNull(asset.getDurableMonths())||asset.getDurableMonths()<12){
                    throw new RuntimeException("原值或预计使用期数不满一年！");
                }
                int durableYears = durableMonths / 12; //使用年数 5
                yearMoney = originalValue / durableYears*2;//年折旧额 333

            }
            asset.setDepreciationMonths(1);//已折旧月数
        }else {
            if(asset.getDepreciationMethod() == 0){
                //平均年限法
                yearMoney = asset.getDepreciationMonthValue();//平均月折旧额

            } else if(asset.getDepreciationMethod() == 1){
                Long originalValue = asset.getOriginalValue();//原值
                Integer durableMonths = asset.getDurableMonths();//使用月数
                if(Objects.isNull(asset.getOriginalValue())||Objects.isNull(asset.getDurableMonths())||asset.getDurableMonths()<12){
                    throw new RuntimeException("原值或预计使用期数不满一年！");
                }
                int durableYears = durableMonths / 12; //使用年数
                Integer depreciationMonths = asset.getDepreciationMonths()+1;//已折旧月数 +1表示加上当月

                Long alreadyDepreciation = 0L;//已折旧
                for (AssetDepreciationCertificate assetDepreciationCertificate : assetDepreciationCertificateList) {
                    alreadyDepreciation += assetDepreciationCertificate.getMoney();
                }
                int noDepreciationMonths = durableMonths - depreciationMonths;//未折旧月数
                //如果noDepreciationMonths大于24个月
                if(noDepreciationMonths>24){//当月不是最后两年
                    yearMoney = (originalValue-alreadyDepreciation) / durableYears*2;//年折旧额

                }
                //如果noDepreciationMonths小于等于24个月
                if (noDepreciationMonths<=24){//当月为最后两年
                    Long ratioRemainingValue = asset.getRatioRemainingValue();//预计残值
                    yearMoney = (originalValue-alreadyDepreciation-ratioRemainingValue) / durableYears*2;//年折旧额

                }
                //如果noDepreciationMonths小于12个月
                if (noDepreciationMonths<12){//当月为最后一年
                    //获取assetDepreciationCertificate的最后一个月的折旧额
                    AssetDepreciationCertificate assetDepreciationCertificate = assetDepreciationCertificateList.get(assetDepreciationCertificateList.size()-1);
                    yearMoney = assetDepreciationCertificate.getMoney();
                }
            }
            asset.setDepreciationMonths(asset.getDepreciationMonths()+1);//已折旧月数
        }
        return yearMoney;
    }

}




