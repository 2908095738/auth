package com.bbs.financial.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.financial.entity.Certificate;

/**
 * 出纳业务层
 */
public interface CashierService {

    /**
     * 查询日记账
     *
     * @param current   页码
     * @param size      条数
     * @param companyId 公司id
     * @param zhangHuId 账户id
     * @param dateStr   时间戳字符串
     * @param startDateLong 起始时间时间戳
     * @param endDateLong   结束时间时间戳
     * @param isMonth   true：当月；false：当月及之前
     * @param isPage    true：分页；false：不分页
     */
    Page<Certificate> listCertificate(Integer current, Integer size, Long companyId, Long zhangHuId, String dateStr, Long startDateLong, Long endDateLong, boolean isMonth, boolean isPage);
}