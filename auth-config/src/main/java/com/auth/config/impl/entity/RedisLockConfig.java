package com.auth.config.impl.entity;

import cn.hutool.extra.spring.SpringUtil;
import com.auth.redis.RedisUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import lombok.Data;

import static org.apache.commons.lang3.math.NumberUtils.LONG_ZERO;

/**
 * 配置：分布式锁
 * @TableName config_redis_lock
 */
@TableName(value ="config_redis_lock")
@Data
public class RedisLockConfig implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 标识 code
     */
    private String code;

    /**
     * redis lock key 前缀
     */
    private String keyPrefix;

    /**
     * 等待锁的超时时间（单位毫秒）
     */
    private Long waitTime;

    /**
     * 自动释放锁时间（单位毫秒）
     */
    private Long leaseTime;

    /**
     * 锁描述
     */
    private String descriptor;

    /**
     * 获取锁失败后，是否直接执行（0否；1是）
     */
    private Integer failExec;

    /**
     * 状态（0未启用；1启用）
     */
    private Integer state;

    public String generateKey(Long suffix) {
        return generateKey(suffix.toString());
    }

    public String generateKey(String suffix) {
        return keyPrefix + suffix;
    }

    private String generateAverageUnlockingTimeKey(Long userId) {
        return keyPrefix + "avg:unlock:time:" + userId;
    }

    /**
     * 获取平均解锁时间
     */
    public long getAverageUnlockingTime(Long userId) {
        String key = generateAverageUnlockingTimeKey(userId);
        RedisUtil redisUtil = SpringUtil.getBean(RedisUtil.class);
        return Optional.of(redisUtil.get(key, Long.class)).orElse(LONG_ZERO);
    }

    /**
     * 设置平均解锁时间
     */
    public void setAverageUnlockingTime(Long userId, Long time) {
        String key = generateAverageUnlockingTimeKey(userId);

        long averageUnlockingTime = getAverageUnlockingTime(userId);
        if(averageUnlockingTime < time) {
            RedisUtil redisUtil = SpringUtil.getBean(RedisUtil.class);
            redisUtil.set(key, time, 1, TimeUnit.DAYS);
        }
    }

    public TimeUnit getUnit() {
        return TimeUnit.MILLISECONDS;
    }

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}