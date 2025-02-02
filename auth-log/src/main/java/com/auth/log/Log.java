package com.auth.log;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.auth.config.Config;
import com.auth.config.impl.entity.RedisCacheConfig;
import com.auth.log.entity.LoginLog;
import com.auth.log.impl.LoginLogServiceImpl;
import com.auth.log.util.IpUtil;
import com.auth.redis.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ONE;
import static org.apache.commons.lang3.math.NumberUtils.LONG_ZERO;

public interface Log {

    class RequestID {
        public static final String REQUEST_ID = "TRACE_ID";

        public static String getRequestID() {
            return MDC.get(REQUEST_ID);
        }
    }

    @Slf4j
    class Login {

        private static final String CACHE_CODE = "log_login";

        private static final RedisCacheConfig cacheConfig = SpringUtil.getBean(Config.CacheConfig.class).getKeyConfig(CACHE_CODE);

        /**
         * 记录登录日志
         */
        public static void record(Long userId, Date createTime, Long loginTime) {
            HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
            RedisUtil redisUtil = SpringUtil.getBean(RedisUtil.class);
            String ip = IpUtil.getIp(request);
            LoginLog loginLog = new LoginLog(userId, createTime, ip, RequestID.getRequestID(), loginTime);
            redisUtil.zSet(cacheConfig.generateKey(), JSONUtil.toJsonStr(loginLog), userId.doubleValue());
        }

        public static final Integer SCAN_BATCH_NUMBER = 1000;

        public static Boolean clearTodayLogs() {
            log.info("定时任务 - 登录日志 - 批量保存: 任务开始执行...");
            String redisKey = cacheConfig.generateKey();
            LoginLogServiceImpl loginLogService = SpringUtil.getBean(LoginLogServiceImpl.class);
            DataSourceTransactionManager transactionManager = SpringUtil.getBean(DataSourceTransactionManager.class);
            TransactionDefinition transactionDefinition = SpringUtil.getBean(TransactionDefinition.class);
            RedisTemplate<String, String> redisTemplate = SpringUtil.getBean(new TypeReference<RedisTemplate<String, String>>() {});
            TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
            try {
                Long total = redisTemplate.opsForZSet().size(redisKey);
                if(nonNull(total)) {
                    long pageTotal = computerTotalPage(total);
                    if(pageTotal > LONG_ZERO) {
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
                        log.info("定时任务 - 登录日志 - 批量保存: 任务执行结束！共{}条数据，分{}批", total, pageTotal);
                    }
                } else {
                    log.info("定时任务 - 登录日志 - 批量保存: 任务无需执行，缓存无日志！");
                }
                transactionManager.commit(transaction);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                transactionManager.rollback(transaction);
                return false;
            }
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
            return ScanOptions.scanOptions().count(Login.SCAN_BATCH_NUMBER).build();
        }
    }
}
