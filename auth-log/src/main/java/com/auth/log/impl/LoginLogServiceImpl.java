package com.auth.log.impl;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.IdUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.auth.config.Config;
import com.auth.config.impl.entity.RedisCacheConfig;
import com.auth.config.impl.entity.RedisLockConfig;
import com.auth.log.Log;
import com.auth.log.entity.LoginLog;
import com.auth.log.mapper.LoginLogMapper;
import com.auth.log.util.IpUtil;
import com.auth.redis.RedisUtil;
import com.auth.vo.BasePageParam;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.base.MPJBaseServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ONE;
import static org.apache.commons.lang3.math.NumberUtils.LONG_ZERO;

@Slf4j
@Primary
@Service
public class LoginLogServiceImpl extends MPJBaseServiceImpl<LoginLogMapper, LoginLog> implements Log.Login {

    /**
     * 从 Redis 中，获取日志缓存的批数量
     * （使用 scan 命令，每次最多从 Redis 中读取 1000 条数据）
     */
    public static final Integer SCAN_BATCH_NUMBER = 1000;

    private static final String CACHE_CODE = "log_login";

    private static final RedisCacheConfig cacheConfig = SpringUtil.getBean(Config.CacheConfig.class).getKeyConfig(CACHE_CODE);

    private static final RedisLockConfig taskLockConfig = SpringUtil.getBean(Config.LockConfig.class).getConfig("lock_task_log_login_clean");


    @Resource
    private HttpServletRequest httpServletRequest;

    @Resource
    private RedisUtil redisUtil;

    @Override
    public void add(Long userId, Date loginTime, Long tripTime) {
        String ip = IpUtil.getIp(httpServletRequest);
        LoginLog loginLog = new LoginLog(userId, loginTime, ip, Log.RequestID.getRequestID(), tripTime);
        long score = loginTime.getTime() + IdUtil.getSnowflakeNextId();
        redisUtil.zSet(cacheConfig.generateKey(), JSONUtil.toJsonStr(loginLog), (double) score);
    }

    @Override
    public Boolean saveAndCleanTodayLogs() {
        log.info("定时任务 - 登录日志 - 批量保存: 任务开始执行...");
        String redisKey = cacheConfig.generateKey();
        LoginLogServiceImpl loginLogService = SpringUtil.getBean(LoginLogServiceImpl.class);
        DataSourceTransactionManager transactionManager = SpringUtil.getBean(DataSourceTransactionManager.class);
        TransactionDefinition transactionDefinition = SpringUtil.getBean(TransactionDefinition.class);
        RedisTemplate<String, String> redisTemplate = SpringUtil.getBean(new TypeReference<RedisTemplate<String, String>>() {});
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);

        RLock lock = getLock();
        try {
            if(lock.tryLock(taskLockConfig.getWaitTime(), taskLockConfig.getLeaseTime(), taskLockConfig.getUnit())) {
                log.info("定时任务 - 登录日志 - 批量保存: 成功获取到分布式锁，开始执行...");
                Long total = redisTemplate.opsForZSet().size(redisKey);
                if(nonNull(total) && total > LONG_ZERO) {
                    long pageTotal = computerTotalPage(total);
                    for (int currentPage = INTEGER_ONE; currentPage <= pageTotal; currentPage++) {
                        // 通过 scan 命令，分页读取 zSet 中的日志（每批上限 1000）
                        Cursor<ZSetOperations.TypedTuple<String>> data = page(redisKey);
                        Set<LoginLog> logSet = converter(data);
                        // 批量保存到数据库
                        boolean isSuccess = loginLogService.saveBatch(logSet);
                        if(!isSuccess) {
                            log.error("定时任务 - 登录日志 - 批量保存: 保存失败，任务停止并触发回滚！第 {} 页; 每页 {} 条; 共 {} 页; 总数据量 {} 条;", currentPage, SCAN_BATCH_NUMBER, pageTotal, total);
                            throw new RuntimeException("保存登录日志失败!");
                        }
                    }
                    redisTemplate.opsForZSet().removeRange(redisKey, 0, total - 1);
                    log.info("定时任务 - 登录日志 - 批量保存: 任务执行结束！共{}条数据，分{}批", total, pageTotal);
                } else {
                    log.info("定时任务 - 登录日志 - 批量保存: 任务无需执行，缓存无日志！");
                }
                transactionManager.commit(transaction);
                return true;
            } else {
                log.error("定时任务 - 登录日志 - 批量保存: 任务可能正在执行（无法获取到分布式锁!）放弃执行");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            transactionManager.rollback(transaction);
            return false;
        } finally {
            if(lock.isLocked()) {
                lock.unlock();
            }
        }
    }

    @Override
    public Page<LoginLog> page(BasePageParam param) {
        return super.page(param.toPage());
    }

    private static RLock getLock() {
        RedissonClient redisson = SpringUtil.getBean(RedissonClient.class);
        return redisson.getSpinLock(taskLockConfig.generateKey());
    }

    private static long computerTotalPage(Long total) {
        long totalPage = total / SCAN_BATCH_NUMBER;
        if(total % SCAN_BATCH_NUMBER > 0) {
            totalPage++;
        }
        return totalPage;
    }

    private static Set<LoginLog> converter(Cursor<ZSetOperations.TypedTuple<String>> pageResult) {
        return pageResult.stream().map(zSetOperations -> JSONUtil.toBean(zSetOperations.getValue(), LoginLog.class)).collect(Collectors.toSet());
    }

    private static Cursor<ZSetOperations.TypedTuple<String>> page(String redisKey) {
        RedisTemplate<String, String> redisTemplate = SpringUtil.getBean(new TypeReference<RedisTemplate<String, String>>() {});
        return redisTemplate.opsForZSet().scan(redisKey, buildScanOptions());
    }

    private static ScanOptions buildScanOptions() {
        return ScanOptions.scanOptions().count(SCAN_BATCH_NUMBER).build();
    }
}
