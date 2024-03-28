package com.bbs.auth.cache.impl;

import cn.hutool.core.util.RandomUtil;
import com.bbs.auth.cache.TokenCache;
import com.bbs.auth.enums.RedisKeys;
import com.bbs.auth.enums.ZookeeperNodePaths;
import com.bbs.auth.util.RedisUtil;
import com.bbs.auth.util.ZKUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class TokenCacheImpl implements TokenCache {

    @Resource
    private RedisUtil redis;

    @Resource
    private ZKUtil zkUtil;


    private Integer getTokenTimeOutMax() { return Integer.valueOf(zkUtil.getForPath(ZookeeperNodePaths.CacheConf.Token.TIMEOUT_MAX)); }

    private Integer getTokenTimeOutMin() { return Integer.valueOf(zkUtil.getForPath(ZookeeperNodePaths.CacheConf.Token.TIMEOUT_MIN)); }

    @Override
    public void setToken(Long uid, String token) {
        int timeout = RandomUtil.randomInt(getTokenTimeOutMin(), getTokenTimeOutMax());
        redis.set(RedisKeys.USER_UID_AND_TOKEN_MAP.key(uid), token, timeout, TimeUnit.MINUTES);
    }

    @Override
    public String getToken(Long uid) throws IllegalArgumentException {
        return redis.get(RedisKeys.USER_UID_AND_TOKEN_MAP.key(uid));
    }

    @Override
    public void expireToken(Long uid) {
        int timeout = RandomUtil.randomInt(getTokenTimeOutMin(), getTokenTimeOutMax());
        redis.expire(RedisKeys.USER_UID_AND_TOKEN_MAP.key(uid), timeout, TimeUnit.MINUTES);
    }
}
