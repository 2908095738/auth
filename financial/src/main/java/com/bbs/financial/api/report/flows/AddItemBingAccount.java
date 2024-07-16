package com.bbs.financial.api.report.flows;

import com.bbs.Result;
import com.bbs.financial.entity.Account;
import com.bbs.financial.entity.ReportFlowsAccount;
import com.bbs.financial.mapper.ReportFlowsAccountMapper;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ZERO;

@RestController
@RequestMapping
public class AddItemBingAccount extends MPJBaseServiceImpl<ReportFlowsAccountMapper, ReportFlowsAccount> {

    @Resource
    private SearchItemAccount searchItemAccount;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class Param {

        private Long accountId;

        private String code;
    }


    @PutMapping("/report/flows/account")
    public Result<List<Account>> search(@RequestBody Param param) {
        Preconditions.checkArgument(searchItemAccount.search(param.code).getData().stream()
                .filter(account -> account.getId().equals(param.accountId)).count() == INTEGER_ZERO, "科目已存在");
        save(new ReportFlowsAccount(param.code, param.accountId, 1, 0L));
        return searchItemAccount.search(param.code);
    }
}
