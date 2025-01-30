package com.auth.cache.login.fail.count.impl;

import cn.hutool.extra.spring.SpringUtil;
import com.auth.cache.login.fail.count.LoginFailCount;
import com.auth.config.Config;
import com.auth.config.impl.entity.RedisCacheConfig;
import com.auth.redis.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.math.NumberUtils.LONG_ONE;

@Slf4j
@Order(1)
@Service
public class LoginFailCountImpl implements LoginFailCount, InitializingBean {

    @Resource
    private RedisUtil redisUtil;

    private static final String CACHE_CODE = "login_fail_count";

    private static RedisCacheConfig cacheConfig;

    /**
     * 系统参数：登录失败限制的次数阈值
     */
    private static final String LOGIN_FAIL_THRESHOLD_CODE = "login_fail_threshold";

    private Integer loginFailThreshold;

    /**
     * 系统参数：登录失败限制的统计时间窗口
     */
    private static final String LOGIN_FAIL_TIME_WINDOW_CODE = "login_fail_time_window";

    private Integer loginFailTimeWindow;

    @Override
    public void afterPropertiesSet() {
        cacheConfig = SpringUtil.getBean(Config.CacheConfig.class).getKeyConfig(CACHE_CODE);
        loginFailThreshold = SpringUtil.getBean(Config.SystemConfig.class).getConfig(LOGIN_FAIL_THRESHOLD_CODE).getIntValue();
        loginFailTimeWindow = SpringUtil.getBean(Config.SystemConfig.class).getConfig(LOGIN_FAIL_TIME_WINDOW_CODE).getIntValue();
    }

    @Override
    public Boolean tryIncr(Long userId) {
        String redisKey = cacheConfig.generateKey(userId);
        Long failNumber = redisUtil.get(redisKey, Long.class);

        if(isNull(failNumber)) {
            redisUtil.set(redisKey, LONG_ONE.toString(), loginFailTimeWindow, TimeUnit.MINUTES);
        } else {
            if(failNumber < loginFailThreshold) {
                redisUtil.increment(redisKey);
            } else {
                return false;
            }
        }
        return true;
    }
}
