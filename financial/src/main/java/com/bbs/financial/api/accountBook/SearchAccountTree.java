package com.bbs.financial.api.accountBook;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.financial.entity.CertificateAbstract;
import com.bbs.financial.service.AccountService;
import com.bbs.financial.service.CertificateAbstractService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class SearchAccountTree {

    @Resource
    private CertificateAbstractService certificateAbstractService;

    @Resource
    private AccountService accountService;

    @GetMapping("/certificate/accountTree")
    public Result<List<Tree<Long>>> accountTree(@RequestParam("companyId")Long companyId,
                                                @RequestParam("createTime") String certificateCreateTime) {
        List<Tree<Long>> list = accountService.selectTree(companyId, certificateCreateTime);

        return Result.success(list);
    }

    @GetMapping("/certificate/account/abstract")
    public Result<Page<CertificateAbstract>> accountAbstract(@RequestParam("companyId")Long companyId,
                                                             @RequestParam("createTime") String certificateCreateTime,
                                                             @RequestParam("accountId")Long accountId,
                                                             @RequestParam("current")Integer current,
                                                             @RequestParam("size")Integer size) {
        Page<CertificateAbstract> list = certificateAbstractService.selectPage(companyId, certificateCreateTime, accountId, current, size);
        initData(list);
        return Result.success(list);
    }


    @GetMapping("/certificate/account/general")
    public Result<Page<CertificateAbstract>> accountAbstract(@RequestParam("companyId")Long companyId,
                                                             @RequestParam("createTime") String certificateCreateTime,
                                                             @RequestParam("current")Integer current,
                                                             @RequestParam("size")Integer size) {
        Page<CertificateAbstract> list = certificateAbstractService.selectPage(companyId, certificateCreateTime,null, current, size);
        initData(list);
        List<String> emnu = Lists.newArrayList("期初余额", "本期合计", "本年累计");
        list.setRecords(list.getRecords().stream().filter(item -> emnu.contains(item.getCertificateAbstract())).collect(Collectors.toList()));
        return Result.success(list);
    }


    private void initData(Page<CertificateAbstract> list) {
        if(CollUtil.isNotEmpty(list.getRecords())) {
            List<CertificateAbstract> abstractList = list.getRecords();

            Map<Long, List<CertificateAbstract>> collect = abstractList.stream().collect(Collectors.groupingBy(CertificateAbstract::getAccountId));
            for (Long accountId : collect.keySet()) {

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
                    }
                    if(i == abstractListSorted.size()-1){
                        CertificateAbstract certificateAbstract = abstractListSorted.get(i);
                        borrowMoney = borrowMoney+(certificateAbstract.getBorrowMoney()!=null?certificateAbstract.getBorrowMoney():0L);
                        LoansMoney = LoansMoney+(certificateAbstract.getLoansMoney()!=null?certificateAbstract.getLoansMoney():0L);
                        certificateAbstract.setSurplusMoney(borrowMoney-LoansMoney);

                        initialBalanceAbstract.setAccountId(certificateAbstract.getAccountId());
                        initialBalanceAbstract.setCertificateAbstract("期初余额");
                        initialBalanceAbstract.setAccount(certificateAbstract.getAccount());
                        abstractList.add(initialBalanceAbstract);

                        currentPeriodAbstract.setAccountId(certificateAbstract.getAccountId());
                        currentPeriodAbstract.setCertificateAbstract("本期合计");
                        currentPeriodAbstract.setBorrowMoney(borrowMoney);
                        currentPeriodAbstract.setLoansMoney(LoansMoney);
                        currentPeriodAbstract.setSurplusMoney(borrowMoney-LoansMoney);
                        currentPeriodAbstract.setAccount(certificateAbstract.getAccount());
                        abstractList.add(currentPeriodAbstract);

                        incurredYearAbstract.setAccountId(certificateAbstract.getAccountId());
                        incurredYearAbstract.setCertificateAbstract("本年累计");
                        incurredYearAbstract.setBorrowMoney(borrowMoney);
                        incurredYearAbstract.setLoansMoney(LoansMoney);
                        incurredYearAbstract.setSurplusMoney(borrowMoney-LoansMoney);
                        incurredYearAbstract.setAccount(certificateAbstract.getAccount());
                        abstractList.add(incurredYearAbstract);

                    }else {
                        CertificateAbstract certificateAbstract = abstractListSorted.get(i);
                        borrowMoney = borrowMoney+(certificateAbstract.getBorrowMoney()!=null?certificateAbstract.getBorrowMoney():0L);
                        LoansMoney = LoansMoney+(certificateAbstract.getLoansMoney()!=null?certificateAbstract.getLoansMoney():0L);
                        certificateAbstract.setSurplusMoney(borrowMoney-LoansMoney);
                    }

                }
            }
        }
    }



}
