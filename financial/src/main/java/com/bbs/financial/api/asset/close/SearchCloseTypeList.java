package com.bbs.financial.api.asset.close;

import com.bbs.Result;
import com.bbs.financial.entity.CloseType;
import com.bbs.financial.service.CloseTypeService;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.math.NumberUtils.*;

@RestController
@RequestMapping
public class SearchCloseTypeList {

    @Resource
    private CloseTypeService closeTypeService;
    @Resource
    private TransactionDefinition transactionDefinition;
    @Resource
    private DataSourceTransactionManager transactionManager;

    @GetMapping("/close/type/list")
    public Result<List<CloseType>> search(@RequestParam Long companyId) {
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            List<CloseType> assetCloseTypeList = searchCloseType(companyId);
            transactionManager.commit(transaction);
            return Result.success(assetCloseTypeList);
        } catch (Exception e) {
            transactionManager.rollback(transaction);
            e.printStackTrace();
            return Result.failed(e.getMessage());
        }
    }

    /**
     * 查询结账类型配置
     */
    public List<CloseType> searchCloseType(Long companyId) {
        List<CloseType> closeTypeList = closeTypeService.lambdaQuery()
                .eq(CloseType::getCompanyId, companyId)
                .list();
        if(isNull(closeTypeList) || closeTypeList.size() == INTEGER_ZERO) {
            closeTypeList = loadCloseTypeByDefault(companyId);
        }
        return closeTypeList;
    }

    /**
     * 通过默认结账类型配置，设置当前公司的结账类型配置
     */
    public List<CloseType> loadCloseTypeByDefault(Long companyId) {
        List<CloseType> defaultCloseTypeList = searchDefaultCloseTypeList();
        defaultCloseTypeList.forEach(assetCloseType -> {
            assetCloseType.setId(null);
            assetCloseType.setCompanyId(companyId);
        });
        closeTypeService.saveBatch(defaultCloseTypeList);
        return closeTypeService.lambdaQuery()
                .eq(CloseType::getCompanyId, companyId)
                .list();
    }

    /**
     * 查询默认的结账项类型
     */
    public List<CloseType> searchDefaultCloseTypeList() {
        List<CloseType> defaultCloseTypeList = closeTypeService.lambdaQuery()
                .eq(CloseType::getCompanyId, LONG_ZERO)
                .list();
        if(isNull(defaultCloseTypeList) || defaultCloseTypeList.size() == INTEGER_ZERO) {
            closeTypeService.saveBatch(Arrays.asList(
                    new CloseType(LONG_ZERO, "计提折旧", LONG_ZERO, INTEGER_ONE, INTEGER_ZERO),
                    new CloseType(LONG_ZERO, "转出未交增值税", LONG_ZERO, INTEGER_ZERO, INTEGER_ONE),
                    new CloseType(LONG_ZERO, "计提附加税", LONG_ZERO, INTEGER_ZERO, INTEGER_TWO),
                    new CloseType(LONG_ZERO, "结转销售成本", LONG_ZERO, INTEGER_ONE, 3),
                    new CloseType(LONG_ZERO, "结转损益", LONG_ZERO, INTEGER_ONE, 4)
            ));
            defaultCloseTypeList = closeTypeService.lambdaQuery()
                    .eq(CloseType::getCompanyId, LONG_ZERO)
                    .list();
        }
        return defaultCloseTypeList;
    }
}
