package com.bbs.financial.api.invoice.search;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.financial.dto.InvoiceSubjAuxDto;
import com.bbs.financial.entity.AccountAuxiliary;
import com.bbs.financial.entity.AccountAuxiliaryType;
import com.bbs.financial.service.AccountAuxiliaryService;
import com.bbs.financial.service.AccountAuxiliaryTypeService;
import com.bbs.financial.util.LoginUser;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 获取名为存货的辅助核算分页
 */
@RestController
@RequestMapping
public class SearchAuxByInvoice {
    @Resource
    private AccountAuxiliaryService orm;

    @Resource
    private AccountAuxiliaryTypeService typeService;

    private class StringTIP {
        public static final String SAVE_AUX = "存货";

        public static final String NO_SAVE = "没有名为存货的辅助核算类型";

    }

    /**
     * 获取名为存货的辅助核算分页
     *
     * @param current 页码
     * @param size    条数
     */
    @GetMapping("/invoice/auxiliary/list")
    public Result<Page<InvoiceSubjAuxDto>> search(@RequestParam(defaultValue = "1") Integer current, @RequestParam(defaultValue = "64") Integer size) {
        //获取辅助核算类型id
        Long typeId = typeService.selectJoinOne(Long.class, new MPJLambdaWrapper<AccountAuxiliaryType>()
                .select(AccountAuxiliaryType::getId)
                .eq(AccountAuxiliaryType::getAccountingSetId, LoginUser.getLoginSetId())
                .eq(AccountAuxiliaryType::getIsUserDefined, NumberUtils.INTEGER_ZERO)
                .eq(AccountAuxiliaryType::getName, StringTIP.SAVE_AUX)
        );
        if (Objects.isNull(typeId) || typeId < NumberUtils.INTEGER_ONE)
            return Result.success(new Page());

        return Result.success(orm.selectJoinListPage(new Page(current, size), InvoiceSubjAuxDto.class,
                new MPJLambdaWrapper<AccountAuxiliary>()
                        .select(AccountAuxiliary::getId, AccountAuxiliary::getNo, AccountAuxiliary::getName)
                        .eq(AccountAuxiliary::getTypeId, typeId)
                        .eq(AccountAuxiliary::getAccountingSetId, LoginUser.getLoginSetId())
                        .eq(AccountAuxiliary::getIsDeleted, NumberUtils.INTEGER_ZERO)
        ));
    }
}