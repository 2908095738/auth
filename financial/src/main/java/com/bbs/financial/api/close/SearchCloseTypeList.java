package com.bbs.financial.api.close;

import com.bbs.Result;
import com.bbs.financial.entity.CloseType;
import com.bbs.financial.service.CloseService;
import com.bbs.financial.util.LoginUser;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping
public class SearchCloseTypeList {

    @Resource
    private CloseService closeService;
    @Resource
    private TransactionDefinition transactionDefinition;
    @Resource
    private DataSourceTransactionManager transactionManager;

    @GetMapping("/close/type/list")
    public Result<List<CloseType>> search() {
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            List<CloseType> closeTypeList = closeService.searchCloseType(LoginUser.getLoginSetId());
            transactionManager.commit(transaction);
            return Result.success(closeTypeList);
        } catch (Exception e) {
            transactionManager.rollback(transaction);
            e.printStackTrace();
            return Result.failed(e.getMessage());
        }
    }
}
