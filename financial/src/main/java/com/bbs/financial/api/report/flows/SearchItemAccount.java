package com.bbs.financial.api.report.flows;

import com.bbs.Result;
import com.bbs.financial.entity.Account;
import com.bbs.financial.entity.ReportFlowsAccount;
import com.bbs.financial.mapper.ReportFlowsAccountMapper;
import com.bbs.financial.service.AccountService;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.math.NumberUtils.LONG_ZERO;

@RestController
@RequestMapping
public class SearchItemAccount extends MPJBaseServiceImpl<ReportFlowsAccountMapper, ReportFlowsAccount> {

    @Resource
    private AccountService accountService;

    @GetMapping("/report/flows/account")
    public Result<List<Account>> search(@RequestParam String code) {
        return Result.success(accountService.selectJoinList(Account.class, new MPJLambdaWrapper<Account>()
                .selectAll(Account.class)
                .rightJoin(ReportFlowsAccount.class, ReportFlowsAccount::getAccountId, Account::getId)
                .eq(ReportFlowsAccount::getCode, code)
                .eq(ReportFlowsAccount::getCompanyId, LONG_ZERO)
        ).stream().filter(Objects::nonNull).collect(Collectors.toList()));
    }
}
