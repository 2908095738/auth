package com.auth.log;

import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.auth.config.Config;
import com.auth.config.impl.entity.RedisCacheConfig;
import com.auth.log.entity.LoginLog;
import com.auth.log.util.IpUtil;
import com.auth.redis.RedisUtil;
import org.slf4j.MDC;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Objects;

public interface Log {

    class RequestID {
        public static final String REQUEST_ID = "TRACE_ID";

        public static String getRequestID() {
            return MDC.get(REQUEST_ID);
        }
    }

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
            redisUtil.set(cacheConfig.generateKey(userId), JSONUtil.toJsonStr(loginLog), cacheConfig.generateRandomTimeout(), cacheConfig.getTimeoutUnit());
        }
    }
}
