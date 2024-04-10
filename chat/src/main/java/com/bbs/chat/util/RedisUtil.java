package com.bbs.chat.util;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {

    @Resource(name = "protoStuffTemplate")
    private RedisTemplate<String, String> protoStuffTemplate;

    /**
     * 设置过期时间，单位秒
     * @param key 键的名称
     * @param timeout 过期时间
     * @return 成功：true，失败：false
     */
    public boolean setExpireTime(String key, long timeout) {
        return protoStuffTemplate.expire(key, timeout, TimeUnit.SECONDS);
    }

    /**
     * 通过键删除一个值
     * @param key 键的名称
     */
    public void delete(String key) {
        protoStuffTemplate.delete(key);
    }

    /**
     * 判断key是否存在
     * @param key 键的名称
     * @return 存在：true，不存在：false
     */
    public boolean hasKey(String key) {
        return protoStuffTemplate.hasKey(key);
    }

    /**
     * 数据存储
     * @param key 键
     * @param value 值
     */
    public void set(String key, String value) {
        protoStuffTemplate.boundValueOps(key).set(value);
    }

    /**
     * 数据存储的同时设置过期时间
     * @param key 键
     * @param value 值
     * @param expireTime 过期时间
     */
    public void set(String key, String value, Long expireTime) {
        protoStuffTemplate.boundValueOps(key).set(value, expireTime, TimeUnit.SECONDS);
    }

    /**
     * 数据取值
     * @param key 键
     * @return 查询成功：值，查询失败，null
     */
    public Object get(String key) {
        return protoStuffTemplate.opsForValue().get(key);
    }

    public void incr(String key, Integer v){
        protoStuffTemplate.opsForValue().increment(key, v);
    }

    /**
     * 如果存在则设置
     * @param key 键
     * @return
     */
    public Boolean setIfPresent(String key, String value) {
        return protoStuffTemplate.opsForValue().setIfPresent(key, value);
    }

    //zset
    public void zSet(String key, Set<ZSetOperations.TypedTuple<String>> tuples) {
        protoStuffTemplate.opsForZSet().add(key, tuples);
    }
    public Set<String> zGet(String key, Double startScore, Double endScore) {
        return protoStuffTemplate.opsForZSet().rangeByScore(key, startScore, endScore);
    }

    public Set<String> zGet(String key, Long startScore, Long endScore) {
        return zGet(key, startScore.doubleValue(), endScore.doubleValue());
    }


    //hash
    public void hashSet(String key, Map<String, String> values) {
        protoStuffTemplate.opsForHash().putAll(key, values);
    }

    public Object hashGet(String key, String hashKey) {
        return protoStuffTemplate.opsForHash().get(key, hashKey);
    }

    public List<Object> hashGet(String key, Collection<Object> hashKeys) {
        return protoStuffTemplate.opsForHash().multiGet(key, hashKeys);
    }

    public void hashSet(String key, Object hashKey, String value) {
        protoStuffTemplate.opsForHash().put(key, hashKey, value);
    }

    public void delHash(String key, Object hashKey) {
        protoStuffTemplate.opsForHash().delete(key, hashKey);
    }

}
