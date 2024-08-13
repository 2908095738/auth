package com.bbs.financial.api.invoice.search;

import com.bbs.Result;
import com.bbs.financial.entity.AccountAuxiliary;
import com.bbs.financial.service.AccountAuxiliaryService;
import com.bbs.financial.util.LoginUser;
import com.bbs.financial.util.SpringUtil;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 查询辅助核算
 */
@Controller
@RequestMapping("/invoice")
public class SearchSubjAux {
    @Resource
    private AccountAuxiliaryService orm;

    /**
     * 查询辅助核算列表
     *
     * @param typeId 辅助核算类型id
     */
    @ResponseBody
    @GetMapping("/aux/{typeId}")
    public Result<List<AccountAuxiliary>> search(@PathVariable Long typeId) {
        return Result.success(orm.selectJoinList(AccountAuxiliary.class, new MPJLambdaWrapper<AccountAuxiliary>()
                .eq(AccountAuxiliary::getAccountingSetId, LoginUser.getLoginSetId())
                .eq(AccountAuxiliary::getIsDeleted, NumberUtils.INTEGER_ZERO)
                .eq(AccountAuxiliary::getTypeId, typeId)));
    }

    /**
     * 查询辅助核算分组
     *
     * @param typeIds 伪辅助核算列表字符串
     * @return key: 辅助核算类型id
     */
    @ResponseBody
    @GetMapping("/aux")
    public Result<Map<Long, List<AccountAuxiliary>>> search(@RequestParam String typeIds) {
        return Result.success(orm.selectJoinList(AccountAuxiliary.class, new MPJLambdaWrapper<AccountAuxiliary>()
                        .eq(AccountAuxiliary::getAccountingSetId, LoginUser.getLoginSetId())
                        .eq(AccountAuxiliary::getIsDeleted, NumberUtils.INTEGER_ZERO)
                        .in(AccountAuxiliary::getTypeId, SpringUtil.str2ListByQs(typeIds, Long::valueOf)))
                .stream()
                .collect(Collectors.groupingBy(AccountAuxiliary::getTypeId)));
    }
}