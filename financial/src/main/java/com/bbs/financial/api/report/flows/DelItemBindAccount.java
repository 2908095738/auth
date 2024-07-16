package com.bbs.financial.api.report.flows;

import com.bbs.Result;
import com.bbs.financial.entity.Account;
import com.bbs.financial.entity.ReportFlowsAccount;
import com.bbs.financial.mapper.ReportFlowsAccountMapper;
import com.github.yulichang.base.MPJBaseServiceImpl;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping
public class DelItemBindAccount extends MPJBaseServiceImpl<ReportFlowsAccountMapper, ReportFlowsAccount> {

    @Resource
    private SearchItemAccount searchItemAccount;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class Param {

        private Long accountId;

        private String code;
    }


    @DeleteMapping("/report/flows/account")
    public Result<List<Account>> delete(@RequestBody Param param) {
        lambdaUpdate()
                .eq(ReportFlowsAccount::getAccountId, param.accountId)
                .eq(ReportFlowsAccount::getCode, param.code)
                .remove();
        return searchItemAccount.search(param.code);
    }
}
