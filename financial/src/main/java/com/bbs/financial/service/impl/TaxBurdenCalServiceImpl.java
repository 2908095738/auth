package com.bbs.financial.service.impl;

import com.bbs.financial.entity.TaxBurdenCal;
import com.bbs.financial.service.TaxBurdenCalService;
import com.bbs.financial.mapper.TaxBurdenCalMapper;
import com.bbs.financial.util.LoginUser;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.List;

/**
 * @author Mafty
 * @description 针对表【tax_burden_cal(税负测算)】的数据库操作Service实现
 * @createDate 2024-07-30 14:43:08
 */
@Service
public class TaxBurdenCalServiceImpl extends MPJBaseServiceImpl<TaxBurdenCalMapper, TaxBurdenCal>
        implements TaxBurdenCalService {
    @Override
    public List<TaxBurdenCal> search(List<Long> dates) {
        return selectJoinList(TaxBurdenCal.class,
                new MPJLambdaWrapper<TaxBurdenCal>()
                        .eq(TaxBurdenCal::getAccountingSetId, LoginUser.getLoginSetId())
                        .and(!ObjectUtils.isEmpty(dates), ext -> ext
                                .ge(TaxBurdenCal::getDate, new Date(dates.get(0)))
                                .lt(TaxBurdenCal::getDate, new Date(dates.get(1)))
                        )
                        .orderByDesc(TaxBurdenCal::getType, TaxBurdenCal::getSort));
    }
}