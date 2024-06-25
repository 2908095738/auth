package com.bbs.financial.api.close;

import com.bbs.Result;
import com.bbs.financial.entity.Asset;
import com.bbs.financial.entity.AssetDepreciationCertificate;
import com.bbs.financial.entity.CloseType;
import com.bbs.financial.service.AssetDepreciationCertificateService;
import com.bbs.financial.service.AssetService;
import com.bbs.financial.service.CloseService;
import com.bbs.financial.service.CloseTypeService;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ZERO;
import static org.apache.commons.lang3.math.NumberUtils.LONG_ZERO;

@RestController
@RequestMapping
public class RecomputeMoney {

    @Resource
    private AssetService assetService;
    @Resource
    private AssetDepreciationCertificateService assetDepreciationCertificateService;
    @Resource
    private CloseService closeService;
    @Resource
    private TransactionDefinition transactionDefinition;
    @Resource
    private DataSourceTransactionManager transactionManager;
    @Resource
    private CloseTypeService closeTypeService;

    @PostMapping("/close/compute")
    public Result<List<CloseType>> recomputeMoney(@RequestParam Long companyId) {
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            List<CloseType> closeTypeList = closeService.searchCloseType(companyId);
            closeTypeList.forEach(closeType -> {
                if(closeType.getTypeName().equals("asset_depreciation")) {
                    Long allAssetMoney = computeAllAssetMoney(companyId);
                    closeType.setMoney(allAssetMoney);
                    closeTypeService.lambdaUpdate()
                            .eq(CloseType::getId, closeType.getId())
                            .set(CloseType::getMoney, allAssetMoney)
                            .update();
                }
            });
            transactionManager.commit(transaction);
            return Result.success(closeTypeList);
        } catch (Exception e) {
            transactionManager.rollback(transaction);
            e.printStackTrace();
            return Result.failed(e.getMessage());
        }
    }

    private Long computeAllAssetMoney(Long companyId) {

        List<Asset> allAsset = searchAllAsset(companyId);

        List<AssetDepreciationCertificate> allAssetCertificate = searchAllAssetCertificate(allAsset);

        Map<Long, List<AssetDepreciationCertificate>> assetIdMap = groupCertificateByAssetId(allAssetCertificate);

        return allAsset.stream()
                .mapToLong(asset -> assetService.computeMoney(asset, getAssetCertificate(asset, assetIdMap)))
                .sum();
    }

    private List<AssetDepreciationCertificate> getAssetCertificate(Asset asset, Map<Long, List<AssetDepreciationCertificate>> assetIdMap) {
        return isNull(assetIdMap) ? null : assetIdMap.get(asset.getId());
    }

    private Map<Long, List<AssetDepreciationCertificate>> groupCertificateByAssetId(List<AssetDepreciationCertificate> allAssetCertificate) {
        if(allAssetCertificate.size() > INTEGER_ZERO) {
            return allAssetCertificate.stream()
                    .collect(Collectors.groupingBy(AssetDepreciationCertificate::getAssetId));
        }
        return null;
    }

    private List<AssetDepreciationCertificate> searchAllAssetCertificate(List<Asset> assetList) {
        List<Long> assetIds = assetList.stream().map(Asset::getId).collect(Collectors.toList());
        return assetDepreciationCertificateService.lambdaQuery()
                .in(AssetDepreciationCertificate::getAssetId, assetIds)
                .list();
    }

    private List<Asset> searchAllAsset(Long companyId) {
        return assetService.lambdaQuery()
                .eq(Asset::getCompanyId, companyId)
                .eq(Asset::getStatus, LONG_ZERO)
                .list();
    }
}
