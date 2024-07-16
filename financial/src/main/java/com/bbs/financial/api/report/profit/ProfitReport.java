package com.bbs.financial.api.report.profit;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bbs.Result;
import com.bbs.financial.entity.Account;
import com.bbs.financial.entity.Certificate;
import com.bbs.financial.entity.CertificateAbstract;
import com.bbs.financial.service.AccountService;
import com.bbs.financial.service.CertificateAbstractService;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.ToLongFunction;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ONE;

@RestController
@RequestMapping
public class ProfitReport {

    @Resource
    private CertificateAbstractService certificateAbstractService;
    @Resource
    private AccountService accountService;

    @GetMapping("/report/profit")
    public Result<Boolean> count(
            @RequestParam(value = "startDate") Long startDateLong,
            @RequestParam(value = "endDate") Long endDateLong
    ) {

        // 1.1 销售商品、提供劳务收到的现金
        // 公式：主营业务收入 + 其他业务收入 + 应收账款期初余额 - 应收账款期末余额 + 应收票据期初余额 - 应收票据期末余额


        // 1.1.1 主营业务收入 = 本期销售商品、提供劳务所确认的收入

        Long mainBusinessIncome = mainBusinessIncomeAll(startDateLong, endDateLong);    //营业收入
        Long operatingCosts = operatingCosts(startDateLong, endDateLong);   //营业成本
        Long taxesAndSurcharges = taxesAndSurcharges(startDateLong, endDateLong); //税金及附加
        Long sellingExpenses = sellingExpenses(startDateLong, endDateLong);   //销售费用
        Long administrationExpense = administrationExpense(startDateLong, endDateLong);   //管理费用
        Long financialExpenses = financialExpenses(startDateLong, endDateLong);   //财务费用
        Long assetImpairmentLoss = assetImpairmentLoss(startDateLong, endDateLong);   //资产减值损失

        Long soundValueFlexibleLossAndProfit = soundValueFlexibleLossAndProfit(startDateLong, endDateLong);   //公允价值变动损益

        // 营业收入 - 营业成本 - 税金及附加 - 销售费用 - 管理费用 - 财务费用-资产减值损失+公允价值变动收益(-公允价值变动损失)+投资收益(-投资损失)+其他收益
//        mainBusinessIncome - operatingCosts - taxesAndSurcharges - sellingExpenses - administrationExpense - financialExpenses - assetImpairmentLoss
//                + soundValueFlexibleLossAndProfit

        return null;
    }

    /**
     * 主营业务收入
     */
    private Long mainBusinessIncomeAll(Long startDateLong, Long endDateLong) {
        return sumMoney(Collections.singletonList(6001), startDateLong, endDateLong, CertificateAbstract::getLoansMoney);
    }

    /**
     * 营业成本
     */
    private Long operatingCosts(Long startDateLong, Long endDateLong) {
        return sumMoney(Arrays.asList(6401, 6402), startDateLong, endDateLong, CertificateAbstract::getBorrowMoney);
    }

    /**
     * 税金及附加
     */
    private Long taxesAndSurcharges(Long startDateLong, Long endDateLong) {
        return sumMoney(Collections.singletonList(6403), startDateLong, endDateLong, CertificateAbstract::getBorrowMoney);
    }

    /**
     * 销售费用
     */
    private Long sellingExpenses(Long startDateLong, Long endDateLong) {
        return sumMoney(Collections.singletonList(6601), startDateLong, endDateLong, CertificateAbstract::getBorrowMoney);
    }

    /**
     * 管理费用
     */
    private Long administrationExpense(Long startDateLong, Long endDateLong) {
        return sumMoney(Collections.singletonList(6602), startDateLong, endDateLong, CertificateAbstract::getBorrowMoney);
    }

    /**
     * 财务费用
     */
    private Long financialExpenses(Long startDateLong, Long endDateLong) {
        return sumMoney(Collections.singletonList(6603), startDateLong, endDateLong, CertificateAbstract::getBorrowMoney);
    }

    /**
     * 公允价值变动损益 = 期末公允价值 − 期初公允价值 − 本期买入成本 + 本期卖出收入
     */
    private Long soundValueFlexibleLossAndProfit(Long startDateLong, Long endDateLong) {

        // 期初公允价值 = 期初交易性金融资产公允价值 − 期初交易性金融负债公允价值
        // 对应科目：1101 交易性金融资产 > 1101 公允价值变动
        // 获取 1101 交易性金融资产：公允价值变动的期初余额
        // 获取 2101 交易性金融负债：公允价值变动的期初余额

        //公允价值变动损益(按【借 -> 减】【贷 -> 增】计算)
        return compute(Collections.singletonList(6101L), startDateLong, endDateLong, certificateAbstract -> {
            Long loansMoney = certificateAbstract.getLoansMoney();  //贷方金额
            if(nonNull(loansMoney)) {
                return loansMoney;
            } else {
                return -certificateAbstract.getBorrowMoney();
            }
        });
    }

    /**
     * 资产减值损失
     */
    private Long assetImpairmentLoss(Long startDateLong, Long endDateLong) {
        return sumMoney(Collections.singletonList(6701), startDateLong, endDateLong, CertificateAbstract::getBorrowMoney);
    }

//    /**
//     * 资产减值损失
//     */
//    private Long assetImpairmentLoss(Long startDateLong, Long endDateLong) {
//        return sumMoney(Collections.singletonList(6111), startDateLong, endDateLong, CertificateAbstract::getBorrowMoney);
//    }
//
//    /**
//     * 资产减值损失
//     */
//    private Long assetImpairmentLoss(Long startDateLong, Long endDateLong) {
//        return sumMoney(Collections.singletonList(6711), startDateLong, endDateLong, CertificateAbstract::getBorrowMoney);
//    }

    private Long sumMoney(List<Integer> accountNos, Long startDateLong, Long endDateLong, ToLongFunction<? super CertificateAbstract> mapper) {
        List<Long> accountIds = accountService.listObjs(new LambdaQueryWrapper<Account>()
                .select(Account::getId)
                .in(Account::getNo, accountNos)
        );
        return compute(accountIds, startDateLong, endDateLong, mapper);
    }

    private Long compute(List<Long> accountIds, Long startDateLong, Long endDateLong, ToLongFunction<? super CertificateAbstract> mapper) {
        Date now = new Date();
        return certificateAbstractService.selectJoinList(CertificateAbstract.class, new MPJLambdaWrapper<CertificateAbstract>()
                        .select(CertificateAbstract::getLoansMoney)
                        .rightJoin(Certificate.class, Certificate::getId, CertificateAbstract::getCertificateId)
                        // 查询范围
                        .in(CertificateAbstract::getAccountId, accountIds)
                        // 查询时间
                        .ge(Certificate::getCreateTime, nonNull(startDateLong) ? new Date(startDateLong) : DateUtil.beginOfMonth(now))
                        .lt(Certificate::getCreateTime, nonNull(endDateLong) ? new Date(endDateLong) : DateUtil.offsetMonth(now, INTEGER_ONE))
                // 取借方金额，并求和
        ).stream().mapToLong(mapper).sum();
    }
}
