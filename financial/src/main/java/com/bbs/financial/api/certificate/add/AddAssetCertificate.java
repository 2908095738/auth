package com.bbs.financial.api.certificate.add;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.bbs.Result;
import com.bbs.enums.financial.CertificateWordEnum;
import com.bbs.financial.entity.Asset;
import com.bbs.financial.entity.AssetDepreciationCertificate;
import com.bbs.financial.entity.Certificate;
import com.bbs.financial.entity.CertificateAbstract;
import com.bbs.financial.service.AssetDepreciationCertificateService;
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
import java.util.Objects;

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
    private AssetDepreciationCertificateService assetDepreciationCertificateService;

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
                Long no = db.lambdaQuery().eq(Certificate::getCompanyId, param.getCompanyId())
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
                    certificate.setType(param.certificateType);
                    db.save(certificate);

                    //借
                    CertificateAbstract borrow = new CertificateAbstract();
                    borrow.setCertificateId(certificate.getId());
                    //贷
                    CertificateAbstract loan = new CertificateAbstract();
                    loan.setCertificateId(certificate.getId());

                    switch (param.certificateType){
                        case 1:
                            //购入凭证
                            if(Objects.isNull(asset.getFixedAssetsAccountId())||Objects.isNull(asset.getPurchaseAssetsOtherPartAccountId())){
                                return Result.failed("购入凭证必须填写固定资产科目和购入其他科目");
                            }
                            borrow.setCertificateAbstract("购入"+asset.getName());
                            borrow.setAccountId(asset.getFixedAssetsAccountId());
                            borrow.setBorrowMoney(asset.getOriginalValue());
                            loan.setCertificateAbstract("购入"+asset.getName());
                            loan.setAccountId(asset.getPurchaseAssetsOtherPartAccountId());
                            loan.setLoansMoney(asset.getOriginalValue());

                            asset.setAssetsCertificateId(certificate.getId());
                            break;
                        case 2:
                            //设置折旧凭证实体数据
                            initCertificateAbstracts(asset,certificate.getId(), borrow, loan);
                            break;
                        case 3:
                            //减值凭证
                            if(Objects.isNull(asset.getImpairmentAccountId())||Objects.isNull(asset.getImpairmentOtherPartAccountId())){
                                return Result.failed("减值凭证必须填写减值科目和减值其他科目");
                            }
                            borrow.setCertificateAbstract("减值"+asset.getName());
                            borrow.setAccountId(asset.getImpairmentOtherPartAccountId());

                            loan.setCertificateAbstract("减值"+asset.getName());
                            loan.setAccountId(asset.getImpairmentAccountId());

                            //TODO wmy 减值金额
                            borrow.setBorrowMoney(asset.getOriginalValue());
                            loan.setLoansMoney(asset.getOriginalValue());

                            asset.setImpairmentCertificateId(certificate.getId());
                            break;
                        case 4:
                            //清理凭证
                            if(Objects.isNull(asset.getAssetsCleanAccountId())||Objects.isNull(asset.getFixedAssetsAccountId())){
                                return Result.failed("清理凭证必须填写清理科目和固定资产科目");
                            }
                            borrow.setCertificateAbstract("清理"+asset.getName());
                            borrow.setAccountId(asset.getAssetsCleanAccountId());
                            borrow.setBorrowMoney(asset.getOriginalValue());
                            loan.setCertificateAbstract("清理"+asset.getName());
                            loan.setAccountId(asset.getFixedAssetsAccountId());
                            loan.setLoansMoney(asset.getOriginalValue());
                            asset.setAssetsCleanCertificateId(certificate.getId());
                            break;
                        case 5:
                            //其他凭证
                            borrow.setCertificateAbstract("其他"+asset.getName());
                            asset.setOtherCertificateId(certificate.getId());
                            break;
                        default:
                            throw new RuntimeException("凭证类型错误");
                    }

                    certificateAbstracts.add(borrow);
                    certificateAbstracts.add(loan);
                    assetService.updateById(asset);
                    no += INTEGER_ONE;
                }
                certificateAbstractService.saveBatch(certificateAbstracts);
            }
            transactionManager.commit(transaction);
            return Result.success();
        } catch (Exception e) {
            transactionManager.rollback(transaction);
            return Result.failed(e.getMessage());
        }
    }


    private void initCertificateAbstracts(Asset asset,Long certificateId, CertificateAbstract borrow, CertificateAbstract loan){
        if(asset.getDepreciationMethod()==3){
            return;
        }
        if(Objects.isNull(asset.getDepreciationAccountId())||Objects.isNull(asset.getDepreciationCostAccountId())){
            throw new RuntimeException("折旧凭证必须填写折旧科目和折旧成本科目");
        }

        List<AssetDepreciationCertificate> assetDepreciationCertificateList = assetDepreciationCertificateService.selectList(asset.getId());

        //根据折旧方法计算出折旧金额
        Long yearMoney = generateMoney(asset, assetDepreciationCertificateList);

        borrow.setCertificateAbstract("折旧"+asset.getName());
        borrow.setAccountId(asset.getDepreciationCostAccountId());
        borrow.setBorrowMoney(yearMoney/12);//月折旧额
        loan.setCertificateAbstract("折旧"+asset.getName());
        loan.setAccountId(asset.getDepreciationAccountId());
        loan.setLoansMoney(yearMoney/12);//月折旧额


        asset.setDepreciationNowMonthValue(yearMoney/12);//月折旧额
        asset.setDepreciationYearValue(yearMoney);//本年折旧额
        asset.setAfterDepreciationAccumulated(asset.getDepreciationMonths()*(yearMoney/12));//期末累计折旧:已折旧月数*平均月折旧额
    //                            asset.setAfterImpairment();//期末减值准备
        asset.setAfterPeriod(asset.getOriginalValue()-asset.getAfterDepreciationAccumulated());//期末净值:原值-期末累计折旧

        Long assetDepreciationCertificateId = null;
        //如果当月有生成，则覆盖
        if(CollUtil.isNotEmpty(assetDepreciationCertificateList)){
            for (AssetDepreciationCertificate assetDepreciationCertificate : assetDepreciationCertificateList) {
                if(assetDepreciationCertificate.getMonth().getMonth() == new Date().getMonth()){
                    assetDepreciationCertificateId = assetDepreciationCertificate.getId();
                }
            }
        }
        //保存到资产的折旧凭证表
        assetDepreciationCertificateService.save(new AssetDepreciationCertificate(assetDepreciationCertificateId,asset.getId(),certificateId,new Date(),yearMoney/12));
    }

    private Long generateMoney(Asset asset, List<AssetDepreciationCertificate> assetDepreciationCertificateList){
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

            }else if(asset.getDepreciationMethod() == 1){
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
