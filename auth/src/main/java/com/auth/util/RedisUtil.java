package com.auth.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.lang.Opt;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Component
public class RedisUtil {

    @Resource(name = "protoStuffTemplate")
    private RedisTemplate<String, String> redis;

    /**
     * 通过键删除一个值
     *
     * @param key 键的名称
     */
    public Boolean delete(String key) {
        return Boolean.TRUE.equals(redis.hasKey(key)) ? redis.delete(key) : Boolean.TRUE;
    }

    /**
     * 数据存储
     * @param key 键
     * @param value 值
     */
    public void set(String key, String value) {
        redis.opsForValue().set(key, value);
    }

    /**
     * 数据存储的同时设置过期时间
     * @param key 键
     * @param value 值
     * @param expireTime 过期时间
     */
    public void set(String key, String value, Long expireTime) {
        redis.boundValueOps(key).set(value, expireTime, TimeUnit.SECONDS);
    }

    /**
     * get
     * @param key 键
     * @return 查询成功：值，查询失败，null
     */
    public <T> T get(String key, Class<T> classes) {
        String str = redis.opsForValue().get(key);
        return StringUtils.isNotBlank(str) ? JSONUtil.toBean(str, classes) : null;
    }

    public String get(String key) {
        return redis.opsForValue().get(key);
    }

    /**
     * get
     * @param key 键
     * @return 为空则返回 Opt<Null>，不空返回 Opt
     */
    public Opt<String> getOpt(String key) {
        return Opt.ofNullable(redis.opsForValue().get(key));
    }

    public void hashSet(String key, Map<String, String> values) {
        redis.opsForHash().putAll(key, values);
    }

    public Object hashGet(String key, String hashKey) {
        return redis.opsForHash().get(key, hashKey);
    }

    public List<Object> hashGet(String key, Collection<Object> hashKeys) {
        return redis.opsForHash().multiGet(key, hashKeys);
    }

    public void hashSet(String key, Object hashKey, String value) {
        redis.opsForHash().put(key, hashKey, value);
    }

    public void multiSet(Map<String, String> map) {
        redis.opsForValue().multiSet(map);
    }

    /**
     * set
     * @param key Redis key
     * @param data Data
     * @param timeout 超时时间
     * @param unit 超时时间单位
     */
    public void set(String key, Object data, int timeout, TimeUnit unit) {
        redis.opsForValue().set(key, JSONUtil.toJsonPrettyStr(data), timeout, unit);
    }

    public void zSet(String key, Set<ZSetOperations.TypedTuple<String>> tuples) {
        redis.opsForZSet().add(key, tuples);
    }

    public Set<String> zGet(String key, Double startScore, Double endScore) {
        return redis.opsForZSet().rangeByScore(key, startScore, endScore);
    }

    public Set<String> zGet(String key, Long startScore, Long endScore) {
        return zGet(key, startScore.doubleValue(), endScore.doubleValue());
    }

    @Slf4j
    public static class Redisson {

        /**
         * 暴力解锁
         */
        public static void forceUnlock(RLock lock) {
            lock.forceUnlock();
            log.warn("Redisson: 暴力解除 lock={}", lock.getName());
        }

        /**
         * 加锁执行代码
         * @param function 代码
         * @param lock Redisson Lock
         * @param waitTime  等待获取锁时间
         * @param leaseTime 自动解锁时间
         * @param unit 时间单位
         * @return 代码执行结果
         * @param <R> 执行结果类型
         */
        public static <R> R lockExec(RLock lock, int waitTime, int leaseTime, TimeUnit unit, Supplier<R> function) {
            try {
                if(lock.tryLock(waitTime, leaseTime, unit)) {
                    log.debug("Redisson: 获取锁 key={}", lock.getName());
                    try {
                        return function.get();
                    } finally {
                        if(lock.isLocked()) {
                            lock.unlock();
                            log.debug("Redisson: 释放锁 key={}", lock.getName());
                        }
                    }
                }
            } catch (InterruptedException e) {
                log.error("Redisson: 分布式锁，中断异常！！！key={}", lock.getName());
                e.printStackTrace();
            }
            if(lock.getHoldCount() > 0) forceUnlock(lock);
            return function.get();
        }


        /**
         * 加锁执行代码
         * @param function 代码
         * @param lock Redisson Lock
         * @param waitTime  等待获取锁时间
         * @param leaseTime 自动解锁时间
         * @param unit 时间单位
         */
        public static void lockExec(RLock lock, int waitTime, int leaseTime, TimeUnit unit, Runnable function) {
            try {
                if(lock.tryLock(waitTime, leaseTime, unit)) {
                    log.debug("Redisson: 获取锁 key={}", lock.getName());
                    try {
                        TimeInterval timer = DateUtil.timer();
                        function.run();
                        long interval = timer.interval();
                        log.info("Redisson: 执行完成 key={}; 耗时（毫秒）={}", lock.getName(), interval);
                        timer.interval();
                        return;
                    } finally {
                        if(lock.isLocked()) {
                            lock.unlock();
                            log.debug("Redisson: 释放锁 key={}", lock.getName());
                        }
                    }
                }
            } catch (InterruptedException e) {
                log.error("Redisson: 分布式锁，中断异常！！！key={}", lock.getName());
                e.printStackTrace();
            }
            if(lock.getHoldCount() > 0) forceUnlock(lock);
            function.run();
        }
    }
}
