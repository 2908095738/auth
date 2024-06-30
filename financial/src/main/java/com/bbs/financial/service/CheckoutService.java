package com.bbs.financial.service;

import com.bbs.Result;
import com.bbs.financial.entity.Checkout;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.List;

/**
 * @author Mafty
 * @description 针对表【checkout(结账)】的数据库操作Service
 * @createDate 2024-06-01 12:06:42
 */
public interface CheckoutService extends IService<Checkout> {
    /**
     * 获取出纳启用期间
     *
     * @param companyId 公司id
     * @return
     */
    Result<Date> getOriByCheck(@RequestBody Long companyId);

    /**
     * 获取本年结账列表
     *
     * @param oriDateByYear 本年起始日
     * @param endDateByYear 本年结束日
     * @return
     */
    List<Checkout> getCheckByYear( Date oriDateByYear, Date endDateByYear);

    /**
     * 本月是否结账
     *
     * @param msecStr   时间戳字符串
     * @return
     */
    boolean isCheck( @RequestParam String msecStr);
}