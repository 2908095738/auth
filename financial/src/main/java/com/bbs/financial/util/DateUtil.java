package com.bbs.financial.util;

import cn.hutool.core.date.DateTime;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

public final class DateUtil {

    private DateUtil() {
        throw new RuntimeException("no new obj");
    }

    /**
     * 获取当月起始、结束时间戳列表
     *
     * @param msec 毫秒数
     */
    public static List<Long> getMonthRange(Long msec) {
        DateTime tmp = DateTime.of(msec);
        DateTime leftMonth = cn.hutool.core.date.DateUtil.beginOfMonth(tmp);
        DateTime rightMonth = cn.hutool.core.date.DateUtil.endOfMonth(tmp);
        return Arrays.asList(leftMonth.getTime(), rightMonth.getTime());
    }

    /**
     * 获取当月起始、结束日
     *
     * @param startDateLong 起始时间时间戳
     * @param endDateLong   结束时间时间戳
     */
    public static String getMonthRange(Long startDateLong, Long endDateLong) {
        DateTime leftMonth = cn.hutool.core.date.DateUtil.date(startDateLong);
        DateTime rightMonth = cn.hutool.core.date.DateUtil.date(endDateLong);
        return String.format("%s 至 %s", leftMonth.toDateStr(), rightMonth.toDateStr());
    }

    /**
     * 获取Date
     *
     * @param dateStr [YYYY-MM-DD]
     */
    public static Date getDate(String dateStr) {
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDate localDate = LocalDate.parse(dateStr);
        ZonedDateTime zdt = localDate.atStartOfDay(zoneId);
        return Date.from(zdt.toInstant());
    }
}