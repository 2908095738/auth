package com.bbs.financial.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.financial.entity.Account;
import com.bbs.financial.entity.Certificate;
import com.bbs.financial.entity.CertificateAbstract;
import com.bbs.financial.mapper.CertificateAbstractMapper;
import com.bbs.financial.service.CertificateAbstractService;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;




/**
* @author 路晨霖
* @description 针对表【certificate_abstract(记账凭证摘要)】的数据库操作Service实现
* @createDate 2024-05-10 23:34:18
*/
@Service
public class CertificateAbstractServiceImpl extends MPJBaseServiceImpl<CertificateAbstractMapper, CertificateAbstract>
    implements CertificateAbstractService{

    @Override
    public Page<CertificateAbstract> selectPage(Long companyId, String certificateCreateTime, Long accountId, Integer current, Integer size) {
        return selectJoinListPage(new Page<>(current, size), CertificateAbstract.class, new MPJLambdaWrapper<CertificateAbstract>()
                .selectAll(CertificateAbstract.class)
                .selectAssociation(Certificate.class, CertificateAbstract::getCertificate)
                .rightJoin(Certificate.class, Certificate::getId, CertificateAbstract::getCertificateId)
                .selectAssociation(Account.class, CertificateAbstract::getAccount)
                .rightJoin(Account.class, Account::getId, CertificateAbstract::getAccountId)
                .eq(Objects.nonNull(accountId),CertificateAbstract::getAccountId,accountId)
                .eq(Certificate::getCompanyId,companyId)
                .like(Certificate::getCreateTime,certificateCreateTime)
                .orderByAsc(Certificate::getCreateTime)
        );
    }

    @Override
    public List<CertificateAbstract> selectList(Long companyId, String certificateCreateTime) {
        return selectJoinList(CertificateAbstract.class, new MPJLambdaWrapper<CertificateAbstract>()
                .selectAll(CertificateAbstract.class)
                .selectAssociation(Certificate.class, CertificateAbstract::getCertificate)
                .rightJoin(Certificate.class, Certificate::getId, CertificateAbstract::getCertificateId)
                .selectAssociation(Account.class, CertificateAbstract::getAccount)
                .rightJoin(Account.class, Account::getId, CertificateAbstract::getAccountId)
                .eq(Certificate::getCompanyId,companyId)
                .like(Certificate::getCreateTime,certificateCreateTime)
                .orderByAsc(Certificate::getCreateTime)
        );
    }


    /**
     * 根据科目no合并数据，生成：期初余额，本期合计，本年合计
     */
    @Override
    public void initDataByNo(Page<CertificateAbstract> list) {
        if(CollUtil.isNotEmpty(list.getRecords())) {
            initDataByNo(list.getRecords());
        }
    }

    @Override
    public void initDataByNo(List<CertificateAbstract> list) {
        Map<String, List<CertificateAbstract>> collect = list.stream().collect(Collectors.groupingBy(o->o.getAccount().getNo()));
        for (String accountId : collect.keySet()) {
            List<CertificateAbstract> abstractListSorted = collect.get(accountId).stream().sorted(Comparator.comparing(o -> o.getCertificate().getCreateTime())).collect(Collectors.toList());
            CertificateAbstract initialBalanceAbstract = new CertificateAbstract(),//期初
                    currentPeriodAbstract = new CertificateAbstract(),//本期
                    incurredYearAbstract = new CertificateAbstract();//本年
            Long borrowMoney = 0L,LoansMoney= 0L;//借和贷
            for (int i = 0; i < abstractListSorted.size(); i++) {
                if(i == 0){
                    CertificateAbstract certificateAbstract = abstractListSorted.get(i);
                    borrowMoney = certificateAbstract.getBorrowMoney()!=null?certificateAbstract.getBorrowMoney():0L;
                    LoansMoney = certificateAbstract.getLoansMoney()!=null?certificateAbstract.getLoansMoney():0L;
                    certificateAbstract.setSurplusMoney(borrowMoney-LoansMoney);
                }else {
                    CertificateAbstract certificateAbstract = abstractListSorted.get(i);
                    borrowMoney = borrowMoney+(certificateAbstract.getBorrowMoney()!=null?certificateAbstract.getBorrowMoney():0L);
                    LoansMoney = LoansMoney+(certificateAbstract.getLoansMoney()!=null?certificateAbstract.getLoansMoney():0L);
                    certificateAbstract.setSurplusMoney(borrowMoney-LoansMoney);
                }
                if(i == abstractListSorted.size()-1){
                    CertificateAbstract certificateAbstract = abstractListSorted.get(i);

                    initialBalanceAbstract.setAccountId(certificateAbstract.getAccountId());
                    initialBalanceAbstract.setCertificateAbstract("期初余额");
                    initialBalanceAbstract.setAccount(certificateAbstract.getAccount());
                    list.add(initialBalanceAbstract);

                    currentPeriodAbstract.setAccountId(certificateAbstract.getAccountId());
                    currentPeriodAbstract.setCertificateAbstract("本期合计");
                    currentPeriodAbstract.setBorrowMoney(borrowMoney);
                    currentPeriodAbstract.setLoansMoney(LoansMoney);
                    currentPeriodAbstract.setSurplusMoney(borrowMoney-LoansMoney);
                    currentPeriodAbstract.setAccount(certificateAbstract.getAccount());
                    list.add(currentPeriodAbstract);

                    incurredYearAbstract.setAccountId(certificateAbstract.getAccountId());
                    incurredYearAbstract.setCertificateAbstract("本年累计");
                    incurredYearAbstract.setBorrowMoney(borrowMoney);
                    incurredYearAbstract.setLoansMoney(LoansMoney);
                    incurredYearAbstract.setSurplusMoney(borrowMoney-LoansMoney);
                    incurredYearAbstract.setAccount(certificateAbstract.getAccount());
                    list.add(incurredYearAbstract);
                }
            }
        }
    }


    /**
     * 根据凭证创建月份合并数据，生成：期初余额，本期合计，本年合计
     */
    @Override
    public void initDataByMonth(List<CertificateAbstract> list) {
        if(CollUtil.isNotEmpty(list)) {
            Map<YearMonth, List<CertificateAbstract>> collect = list.stream().collect(Collectors.groupingBy(o->
                    YearMonth.from(o.getCertificate().getCreateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()))
            );
            for (YearMonth accountId : collect.keySet()) {
                List<CertificateAbstract> abstractListSorted = collect.get(accountId).stream().sorted(Comparator.comparing(o -> o.getCertificate().getCreateTime())).collect(Collectors.toList());
                CertificateAbstract initialBalanceAbstract = new CertificateAbstract(),//期初
                        currentPeriodAbstract = new CertificateAbstract(),//本期
                        incurredYearAbstract = new CertificateAbstract();//本年
                Long borrowMoney = 0L,LoansMoney= 0L;//借和贷
                for (int i = 0; i < abstractListSorted.size(); i++) {
                    if(i == 0){
                        CertificateAbstract certificateAbstract = abstractListSorted.get(i);
                        borrowMoney = certificateAbstract.getBorrowMoney()!=null?certificateAbstract.getBorrowMoney():0L;
                        LoansMoney = certificateAbstract.getLoansMoney()!=null?certificateAbstract.getLoansMoney():0L;
                        certificateAbstract.setSurplusMoney(borrowMoney-LoansMoney);
                    }else {
                        CertificateAbstract certificateAbstract = abstractListSorted.get(i);
                        borrowMoney = borrowMoney+(certificateAbstract.getBorrowMoney()!=null?certificateAbstract.getBorrowMoney():0L);
                        LoansMoney = LoansMoney+(certificateAbstract.getLoansMoney()!=null?certificateAbstract.getLoansMoney():0L);
                        certificateAbstract.setSurplusMoney(borrowMoney-LoansMoney);
                    }
                    if(i == abstractListSorted.size()-1){
                        CertificateAbstract certificateAbstract = abstractListSorted.get(i);

                        initialBalanceAbstract.setAccountId(certificateAbstract.getAccountId());
                        initialBalanceAbstract.setCertificateAbstract("期初余额");
                        initialBalanceAbstract.setAccount(certificateAbstract.getAccount());
                        list.add(initialBalanceAbstract);

                        currentPeriodAbstract.setAccountId(certificateAbstract.getAccountId());
                        currentPeriodAbstract.setCertificateAbstract("本期合计");
                        currentPeriodAbstract.setBorrowMoney(borrowMoney);
                        currentPeriodAbstract.setLoansMoney(LoansMoney);
                        currentPeriodAbstract.setSurplusMoney(borrowMoney-LoansMoney);
                        currentPeriodAbstract.setAccount(certificateAbstract.getAccount());
                        list.add(currentPeriodAbstract);

                        incurredYearAbstract.setAccountId(certificateAbstract.getAccountId());
                        incurredYearAbstract.setCertificateAbstract("本年累计");
                        incurredYearAbstract.setBorrowMoney(borrowMoney);
                        incurredYearAbstract.setLoansMoney(LoansMoney);
                        incurredYearAbstract.setSurplusMoney(borrowMoney-LoansMoney);
                        incurredYearAbstract.setAccount(certificateAbstract.getAccount());
                        list.add(incurredYearAbstract);
                    }
                }
            }
        }
    }

    @Override
    public List<CertificateAbstract> selectList(Long companyId, Date certificateStartCreateTime, Date certificateEndCreateTime, Long accountId) {
        return selectJoinList(CertificateAbstract.class, new MPJLambdaWrapper<CertificateAbstract>()
                .selectAll(CertificateAbstract.class)
                .selectAssociation(Certificate.class, CertificateAbstract::getCertificate)
                .rightJoin(Certificate.class, Certificate::getId, CertificateAbstract::getCertificateId)
                .selectAssociation(Account.class, CertificateAbstract::getAccount)
                .rightJoin(Account.class, Account::getId, CertificateAbstract::getAccountId)
                .eq(Objects.nonNull(accountId),CertificateAbstract::getAccountId,accountId)
                .eq(Certificate::getCompanyId,companyId)
                .ge(Objects.nonNull(certificateStartCreateTime),Certificate::getCreateTime,certificateStartCreateTime)
                .le(Objects.nonNull(certificateEndCreateTime),Certificate::getCreateTime,certificateEndCreateTime)
                .orderByAsc(Certificate::getCreateTime)
        );
    }


}




