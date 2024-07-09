package com.bbs.financial.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.financial.dto.IOTotalDto;
import com.bbs.financial.entity.Certificate;

import java.util.Collection;

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
     * @param zhIdList      账户id列表
     * @param pastDateLong  当前日期之前时间戳
     * @param startDateLong 起始时间时间戳
     * @param endDateLong   结束时间时间戳
     * @param isMonth   true：当月；false：当月及之前
     * @param isPage    true：分页；false：不分页
     */
    Page<Certificate> listCertificate(Integer current, Integer size, Long companyId, Collection<Long> zhIdList, Long pastDateLong, Long startDateLong, Long endDateLong, boolean isMonth, boolean isPage);

    /**
     * 获取收支汇总表仅账户相关实例域有数据的列表
     *
     * @param current 页码
     * @param size    条数
     * @param zhId    账户id
     */
    Page<IOTotalDto> getZhDataByTotal(Integer current, Integer size, Long zhId);
}