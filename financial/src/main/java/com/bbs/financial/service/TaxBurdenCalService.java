package com.bbs.financial.service;

import com.bbs.financial.entity.TaxBurdenCal;
import com.github.yulichang.base.MPJBaseService;

import java.util.List;

/**
 * @author Mafty
 * @description 针对表【tax_burden_cal(税负测算)】的数据库操作Service
 * @createDate 2024-07-30 14:43:08
 */
public interface TaxBurdenCalService extends MPJBaseService<TaxBurdenCal> {
    /**
     * @param dates 期数日期时间戳列表
     */
    List<TaxBurdenCal> search(List<Long> dates);
}