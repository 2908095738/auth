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
    private static class Param {

        /**
         * 公司ID
         */
        private Long companyId;

        /**
         * 日期
         */
        private Date date;

        /**
         * 资产ID
         */
        private List<Long> assetIds;

        /**
         * 凭证类型:1购入凭证 2折旧凭证 3减值凭证 4清理凭证 5其他凭证
         */
        private Integer certificateType;

    }

    /**
     * 生成资产凭证：每个资产一个凭证
     * @param param 根据前端传的凭证类型生成对应的凭证
     * @return
     */
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
                    certificate.setDate(param.date);
                    certificate.setCreateBy(LoginUser.getId());
                    db.save(certificate);

                    AssetAccountCertificate assetAccountCertificate = asset.getAssetAccountCertificate();
                    //借
                    CertificateAbstract borrow = new CertificateAbstract();
                    borrow.setCertificateId(certificate.getId());
                    borrow.setLoansMoney(0L);
                    //贷
                    CertificateAbstract loan = new CertificateAbstract();
                    loan.setCertificateId(certificate.getId());
                    loan.setBorrowMoney(0L);

                    switch (param.certificateType){
                        case 1:
                            //购入凭证
                            borrow.setCertificateAbstract("购入"+asset.getName());
                            borrow.setAccountId(assetAccountCertificate.getFixedAssetsAccountId());
                            borrow.setBorrowMoney(asset.getOriginalValue());
                            loan.setCertificateAbstract("购入"+asset.getName());
                            loan.setAccountId(assetAccountCertificate.getPurchaseAssetsOtherPartAccountId());
                            loan.setLoansMoney(asset.getOriginalValue());
                            break;
                        case 2:
                            //折旧凭证
                            long money = 0L;
                            Integer depreciationMethod = asset.getDepreciationMethod();//折旧方法
                            if(depreciationMethod == 1){
                                //平均年限法
                                money = asset.getDepreciationMonthValue();//平均月折旧额

                            }else if(depreciationMethod == 2){
                            //TODO  money =
                            }
                            borrow.setCertificateAbstract("折旧"+asset.getName());
                            borrow.setAccountId(assetAccountCertificate.getDepreciationCostAccountId());
                            borrow.setBorrowMoney(money);
                            loan.setCertificateAbstract("折旧"+asset.getName());
                            loan.setAccountId(assetAccountCertificate.getDepreciationAccountId());
                            loan.setLoansMoney(money);
                            break;
                        case 3:
                            //减值凭证
                            borrow.setCertificateAbstract("减值"+asset.getName());
                            borrow.setAccountId(assetAccountCertificate.getImpairmentOtherPartAccountId());

                            loan.setCertificateAbstract("减值"+asset.getName());
                            loan.setAccountId(assetAccountCertificate.getImpairmentAccountId());

                            //TODO 减值金额
                            borrow.setBorrowMoney(asset.getOriginalValue());
                            loan.setLoansMoney(asset.getOriginalValue());
                            break;
                        case 4:
                            //清理凭证
                            borrow.setCertificateAbstract("清理"+asset.getName());
                            borrow.setAccountId(assetAccountCertificate.getAssetsCleanAccountId());
                            borrow.setBorrowMoney(asset.getOriginalValue());
                            loan.setCertificateAbstract("清理"+asset.getName());
                            loan.setAccountId(assetAccountCertificate.getFixedAssetsAccountId());
                            loan.setLoansMoney(asset.getOriginalValue());
                            break;
                        case 5:
                            //其他凭证
                            borrow.setCertificateAbstract("其他"+asset.getName());
                            break;
                        default:
                            throw new RuntimeException("凭证类型错误");
                    }
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
