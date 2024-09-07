package com.bbs.auth.controller;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.auth.entity.RenewLog;
import com.bbs.auth.entity.User;
import com.bbs.auth.service.RenewLogService;
import com.bbs.auth.service.UserService;
import com.bbs.auth.util.PageUtil;
import com.bbs.exception.ReLoginException;
import com.bbs.vo.UserVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ONE;
import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ZERO;

/**
 * 支付记录
 */
@RestController
public class RenewLogController {

    @Resource
    private RenewLogService renewLogService;

    @Resource
    private UserService userService;

    @GetMapping("/back/renew/log")
    public Result<Page<RenewLog>> backSearch(@RequestParam(required = false) Long userId, Integer current, Integer size) throws IllegalArgumentException, ReLoginException {
        userService.checkLoginUserIsAdmin();
        Page<RenewLog> result = renewLogService.lambdaQuery()
                .eq(nonNull(userId), RenewLog::getCreateBy, userId)
                .page(new Page<>(current, size));
        List<RenewLog> records = result.getRecords();
        if(result.getRecords().size() > INTEGER_ZERO) {
            Map<Long, User> uidMap = userService.searchMap(result.getRecords().stream().map(RenewLog::getCreateBy).collect(Collectors.toSet()));
            records.forEach(log -> {
                log.setUser(uidMap.get(log.getCreateBy()));
                log.setCreateTimeStr(DateUtil.formatDateTime(log.getCreateTime()));
            });
        }
        return Result.success(result);
    }

    @GetMapping("/back/renew/count/log")
    public Result<Page<RenewLog>> searchCumulativePaymentRecord(
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "10")Integer size
    ) throws IllegalArgumentException, ReLoginException {
        userService.checkLoginUserIsAdmin();
        List<RenewLog> all = renewLogService.list();
        if(nonNull(all) && all.size() > INTEGER_ZERO) {

            Map<Long, BigDecimal> countMap = new HashMap<>();
            Map<Long, Integer> payNumberMap = new HashMap<>();
            all.forEach(log -> {
                BigDecimal countMoney = countMap.getOrDefault(log.getCreateBy(), new BigDecimal(INTEGER_ZERO));
                countMap.put(log.getCreateBy(), countMoney.add(log.getMoney()));
                payNumberMap.put(log.getCreateBy(), payNumberMap.getOrDefault(log.getCreateBy(), INTEGER_ZERO) + INTEGER_ONE);
            });
            Set<Long> userIds = countMap.keySet();
            Map<Long, User> uidMap = userService.searchMap(userIds);
            Set<RenewLog> logs = userIds.stream().map(id -> {
                RenewLog renewLog = new RenewLog();
                renewLog.setMoney(countMap.get(id));
                renewLog.setUser(uidMap.get(id));
                renewLog.setPayNumber(payNumberMap.get(id));
                return renewLog;
            }).collect(Collectors.toSet());
            return Result.success(PageUtil.execPage(current, size, new ArrayList<>(logs)));
        }
        return Result.success(new Page<>());
    }

    @GetMapping("/renew/log/list")
    public Result<Page<RenewLog>> search(Integer current, Integer size) throws IllegalArgumentException, ReLoginException {
        Long id = userService.loginUser().getId();
        Page<RenewLog> result = renewLogService.lambdaQuery()
                .eq(nonNull(id), RenewLog::getCreateBy, id)
                .page(new Page<>(current, size));
        List<RenewLog> records = result.getRecords();
        if(result.getRecords().size() > INTEGER_ZERO) {
            Map<Long, User> uidMap = userService.searchMap(result.getRecords().stream().map(RenewLog::getCreateBy).collect(Collectors.toSet()));
            records.forEach(log -> {
                log.setUser(uidMap.get(log.getCreateBy()));
                log.setCreateTimeStr(DateUtil.formatDateTime(log.getCreateTime()));
            });
        }
        return Result.success(result);
    }
}
