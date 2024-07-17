package com.bbs.financial.service.impl;

import cn.hutool.core.date.DateUtil;
import com.bbs.Result;
import com.bbs.financial.entity.Checkout;
import com.bbs.financial.service.CheckoutService;
import com.bbs.financial.mapper.CheckoutMapper;
import com.bbs.financial.util.LoginUser;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ONE;

/**
 * @author Mafty
 * @description 针对表【checkout(结账)】的数据库操作Service实现
 * @createDate 2024-06-01 12:06:42
 */
@Service
public class CheckoutServiceImpl extends MPJBaseServiceImpl<CheckoutMapper, Checkout>
        implements CheckoutService {

    @Override
    public Result<Date> getOriByCheck(Long accountingSetId) {
        List<Date> dbDates = selectJoinList(Date.class, new MPJLambdaWrapper<Checkout>()
                .select(Checkout::getDate)
                .eq(Checkout::getAccountingSetId, accountingSetId)
                .eq(Checkout::getIsCheckout, 1)
                .orderByAsc(Checkout::getDate)
        );

        if (ObjectUtils.isEmpty(dbDates))
            return Result.success(new Date());

        return Result.success(dbDates.get(0));
    }

    @Override
    public List<Checkout> getCheckByYear(Date oriDateByYear, Date endDateByYear) {
        return selectJoinList(Checkout.class, new MPJLambdaWrapper<Checkout>()
                .selectAll(Checkout.class)
                .eq(Checkout::getAccountingSetId, LoginUser.getLoginSetId())
                .ge(Checkout::getDate, oriDateByYear)
                .lt(Checkout::getDate, endDateByYear)
                .orderByAsc(Checkout::getDate)
        );
    }

    @Override
    public boolean isCheck(String msecStr) {
        Date date = nonNull(msecStr) ? new Date(Long.parseLong(msecStr)) : new Date();
        Long id = selectJoinOne(Long.class, new MPJLambdaWrapper<Checkout>()
                .select(Checkout::getId)
                .eq(Checkout::getIsCheckout, 1)
                .eq(Checkout::getAccountingSetId, LoginUser.getLoginSetId())
                .ge(Checkout::getDate, DateUtil.beginOfMonth(date))
                .lt(Checkout::getDate, DateUtil.beginOfMonth(DateUtil.offsetMonth(date, INTEGER_ONE)))
        );

        return !ObjectUtils.isEmpty(id);
    }
}