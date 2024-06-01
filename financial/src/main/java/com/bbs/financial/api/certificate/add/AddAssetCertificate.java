package com.bbs.financial.api.certificate.add;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.bbs.Result;
import com.bbs.enums.financial.CertificateWordEnum;
import com.bbs.financial.entity.Asset;
import com.bbs.financial.entity.AssetAccountCertificate;
import com.bbs.financial.entity.Certificate;
import com.bbs.financial.entity.CertificateAbstract;
import com.bbs.financial.service.AssetAccountCertificateService;
import com.bbs.financial.service.AssetService;
import com.bbs.financial.service.CertificateAbstractService;
import com.bbs.financial.service.CertificateService;
import com.bbs.financial.util.LoginUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ONE;

@RestController
public class AddAssetCertificate {

    @Resource
    private CertificateService db;
    @Resource
    private CertificateAbstractService certificateAbstractService;
    @Resource
    private TransactionDefinition transactionDefinition;
    @Resource
    private DataSourceTransactionManager transactionManager;
    @Resource
    private AssetService assetService;
    @Resource
    private AssetAccountCertificateService assetAccountCertificateService;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Param {

        /**
         * 公司ID
         */
        private Long companyId;

        /**
         * 资产ID
         */
        private List<Long> assetIds;

    }


    @PutMapping("/asset/certificate")
    public Result<Boolean> add(@RequestBody Param param) {
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            // 获取资产:根据资产ID列表
            List<Asset> assets = assetService.selectJoinList(param.assetIds);
            if(CollUtil.isNotEmpty(assets)){
                List<CertificateAbstract> certificateAbstracts = new ArrayList<>();
                // 凭证号
                long no = db.lambdaQuery().eq(Certificate::getCompanyId, param.getCompanyId())
                        .ge(Certificate::getCreateTime, DateUtil.beginOfMonth(new Date()))
                        .lt(Certificate::getCreateTime, DateUtil.beginOfMonth(DateUtil.offsetMonth(new Date(), INTEGER_ONE)))
                        .count() + INTEGER_ONE;
                for (Asset asset : assets) {
                    Certificate certificate = new Certificate();
                    certificate.setCompanyId(param.companyId);
                    certificate.setCertificateWord(CertificateWordEnum.RECORD);
                    certificate.setNo(no);
                    certificate.setDate(new Date());
                    certificate.setCreateBy(LoginUser.getId());
                    db.save(certificate);

                    AssetAccountCertificate assetAccountCertificate = asset.getAssetAccountCertificate();
                    //借
                    CertificateAbstract borrow = new CertificateAbstract();
                    borrow.setCertificateId(certificate.getId());
                    borrow.setCertificateAbstract("购入"+asset.getName());
                    borrow.setAccountId(assetAccountCertificate.getFixedAssetsAccountId());
                    borrow.setBorrowMoney(asset.getOriginalValue());
                    borrow.setLoansMoney(0L);
                    //贷
                    CertificateAbstract loan = new CertificateAbstract();
                    loan.setCertificateId(certificate.getId());
                    loan.setCertificateAbstract("购入"+asset.getName());
                    loan.setAccountId(assetAccountCertificate.getPurchaseAssetsOtherPartAccountId());
                    loan.setBorrowMoney(0L);
                    loan.setLoansMoney(asset.getOriginalValue());
                    certificateAbstracts.add(borrow);
                    certificateAbstracts.add(loan);

                    no += INTEGER_ONE;

                    assetAccountCertificate.setAssetsCertificateId(certificate.getId());
                    assetAccountCertificateService.save(assetAccountCertificate);
                }
                certificateAbstractService.saveBatch(certificateAbstracts);
            }
            transactionManager.commit(transaction);
            return Result.success();
        } catch (Exception e) {
            transactionManager.rollback(transaction);
            throw new RuntimeException(e);
        }
    }
}
