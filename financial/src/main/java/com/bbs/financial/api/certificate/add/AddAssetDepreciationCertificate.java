package com.bbs.financial.api.certificate.add;

import org.springframework.web.bind.annotation.RestController;

/**
 * 生成折旧凭证：多个资产合并为一个凭证
 */
@RestController
public class AddAssetDepreciationCertificate {
//
//    @Resource
//    private CertificateService db;
//    @Resource
//    private CertificateAbstractService certificateAbstractService;
//    @Resource
//    private TransactionDefinition transactionDefinition;
//    @Resource
//    private DataSourceTransactionManager transactionManager;
//    @Resource
//    private AssetService assetService;
//
//    @Data
//    @NoArgsConstructor
//    @AllArgsConstructor
//    public static class Param {
//
//        /**
//         * 公司ID
//         */
//        private Long companyId;
//
//        /**
//         * 凭证生成时间
//         */
//        private Date createTime;
//
//        /**
//         * 凭证摘要
//         */
//        private String digest;
//
//    }
//
//    /**
//     * 生成折旧凭证：多个资产合并为一个凭证
//     * @param param
//     * @return
//     */
//    @PutMapping("/asset/depreciation/certificate")
//    public Result<Boolean> add(@RequestBody Param param) {
//        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
//        try {
//            // 获取资产:1开始使用日期月份比当前月份小 2排除折旧方法为不计提折旧的资产
//            List<Asset> assets = assetService.selectNowJoinList();
//            if(CollUtil.isNotEmpty(assets)){
//                // 凭证号
//                long no = db.lambdaQuery().eq(Certificate::getCompanyId, param.getCompanyId())
//                        .ge(Certificate::getCreateTime, DateUtil.beginOfMonth(new Date()))
//                        .lt(Certificate::getCreateTime, DateUtil.beginOfMonth(DateUtil.offsetMonth(new Date(), INTEGER_ONE)))
//                        .count() + INTEGER_ONE;
//
//                Certificate certificate = new Certificate();
//                certificate.setCompanyId(param.companyId);
//                certificate.setCertificateWord(CertificateWordEnum.RECORD);
//                certificate.setNo(no);
//                certificate.setDate(param.createTime);
//                certificate.setCreateBy(LoginUser.getId());
//                certificate.setType(INTEGER_ONE);
//                db.save(certificate);
//
//                List<CertificateAbstract> certificateAbstractsBorrow = new ArrayList<>();
//                List<CertificateAbstract> certificateAbstractsLoan = new ArrayList<>();
//                assets.forEach(asset -> {
//                    long money = 0L;
//                    Integer depreciationMethod = asset.getDepreciationMethod();//折旧方法
//                    if(depreciationMethod == 1){
//                        //平均年限法
//                        money = asset.getDepreciationMonthValue();//平均月折旧额
//
//                    }else if(depreciationMethod == 2){
////
//                    }
//                    //借
//                    CertificateAbstract borrow = new CertificateAbstract();
//                    borrow.setCertificateId(certificate.getId());
//                    borrow.setCertificateAbstract(param.digest);
//                    borrow.setAccountId(asset.getDepreciationAccountId());
//                    borrow.setBorrowMoney(money);
//                    borrow.setLoansMoney(0L);
//                    //贷
//                    CertificateAbstract loan = new CertificateAbstract();
//                    loan.setCertificateId(certificate.getId());
//                    loan.setCertificateAbstract(param.digest);
//                    loan.setAccountId(asset.getDepreciationCostAccountId());
//                    loan.setBorrowMoney(0L);
//                    loan.setLoansMoney(money);
//                    certificateAbstractsBorrow.add(borrow);
//                    certificateAbstractsLoan.add(loan);
//                });
//
//                List<CertificateAbstract> certificateAbstractAddList = new ArrayList<>();
//
//                //合并科目id相同的数据
//                certificateAbstractAddList.addAll(new ArrayList<>(certificateAbstractsBorrow.stream().collect(Collectors.toMap(
//                                CertificateAbstract::getAccountId,
//                                a -> a, (o1, o2) -> {
//                                    o1.setBorrowMoney(o1.getBorrowMoney() + o2.getBorrowMoney());
//                                    return o1;
//                                }))
//                        .values()));
//
//                certificateAbstractAddList.addAll(new ArrayList<>(certificateAbstractsLoan.stream().collect(Collectors.toMap(
//                                CertificateAbstract::getAccountId,
//                                a -> a, (o1, o2) -> {
//                                    o1.setLoansMoney(o1.getLoansMoney() + o2.getLoansMoney());
//                                    return o1;
//                                }))
//                        .values()));
//                certificateAbstractService.saveBatch(certificateAbstractAddList);
//            }
//            transactionManager.commit(transaction);
//            return Result.success();
//        } catch (Exception e) {
//            transactionManager.rollback(transaction);
//            throw new RuntimeException(e);
//        }
//    }
}
