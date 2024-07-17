package com.bbs.financial.api.close;

import cn.hutool.core.date.DateUtil;
import com.bbs.Result;
import com.bbs.financial.entity.*;
import com.bbs.financial.service.*;
import com.bbs.financial.util.LoginUser;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.math.NumberUtils.*;

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
    @Resource
    private CertificateService certificateService;

    @PostMapping("/close/compute")
    public Result<List<CloseType>> recomputeMoney() {
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            Long loginSetId = LoginUser.getLoginSetId();
            List<CloseType> closeTypeList = closeService.searchCloseType(loginSetId);
            closeTypeList.forEach(closeType -> {
                if(closeType.getTypeName().equals("asset_depreciation")) {
                    Long allAssetMoney = computeAllAssetMoney(loginSetId);
                    closeType.setMoney(allAssetMoney);
                    closeTypeService.lambdaUpdate()
                            .eq(CloseType::getId, closeType.getId())
                            .set(CloseType::getMoney, allAssetMoney)
                            .update();
                } else if(closeType.getTypeName().equals("transfer_out_unpaid_vat")) {
                    // 1. 获取增值税相关科目，当月产生的凭证
                    Date now = new Date();
                    certificateService.selectJoinList(Certificate.class, new MPJLambdaWrapper<Certificate>()
                            .selectAll(Certificate.class)
                            .selectCollection(CertificateAbstract.class, Certificate::getAbstracts)
                            .leftJoin(CertificateAbstract.class, CertificateAbstract::getCertificateId, Certificate::getId)
                            .leftJoin(Account.class, Account::getId, CertificateAbstract::getAccountId)
                            // 筛选公司
                            .eq(Certificate::getAccountingSetId, loginSetId)
                            // 筛选增值税相关科目
                            .eq(Account::getNo, 2221)
                            // 筛选当月数据
                            .ge(Certificate::getCreateTime, DateUtil.beginOfMonth(now))
                            .lt(Certificate::getCreateTime, DateUtil.beginOfMonth(DateUtil.offsetMonth(now, INTEGER_ONE)))
                    );
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

    private Long computeAllAssetMoney(Long accountingSetId) {

        List<Asset> allAsset = searchAllAsset(accountingSetId);

        List<AssetDepreciationCertificate> allAssetCertificate = searchAllAssetCertificate(allAsset);

        if(allAssetCertificate.size() > INTEGER_ZERO) {
            return allAssetCertificate.stream().mapToLong(AssetDepreciationCertificate::getMoney).sum();
        }
        return LONG_ZERO;
    }

    private List<AssetDepreciationCertificate> searchAllAssetCertificate(List<Asset> assetList) {
        List<Long> assetIds = assetList.stream().map(Asset::getId).collect(Collectors.toList());
        Date now = new Date();
        return assetDepreciationCertificateService.lambdaQuery()
                .in(AssetDepreciationCertificate::getAssetId, assetIds)
                .and(wrapper -> wrapper
                        .ge(AssetDepreciationCertificate::getMonth, DateUtil.beginOfMonth(now))
                        .lt(AssetDepreciationCertificate::getMonth, DateUtil.offsetMonth(now, INTEGER_ONE))
                )
                .list();
    }

    private List<Asset> searchAllAsset(Long accountingSetId) {
        return assetService.lambdaQuery()
                .eq(Asset::getAccountingSetId, accountingSetId)
                .eq(Asset::getStatus, LONG_ZERO)
                .list();
    }
}
