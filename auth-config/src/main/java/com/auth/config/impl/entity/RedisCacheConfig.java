package com.auth.config.impl.entity;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.TimeUnit;

@TableName(value ="config_redis_cache")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RedisCacheConfig {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 配置编码
     */
    @TableField(value = "code")
    private String code;

    /**
     * key 前缀
     */
    @TableField(value = "key_prefix")
    private String keyPrefix;

    /**
     * 过期时间（单位：分钟）
     */
    @TableField(value = "timeout")
    private Integer timeout;

    /**
     * 过期时间范围（1. 单位：分钟；2. 作用：分散 key 过期）
     */
    @TableField(value = "timeout_scope")
    private Integer timeoutScope;

    /**
     * 描述
     */
    @TableField(value = "descriptor")
    private String descriptor;

    /**
     * 状态
     */
    @TableField(value = "state")
    private Integer state;

    public String generateKey(String key) {
        return keyPrefix + key;
    }

    public String generateKey(Long key) {
        return generateKey(key.toString());
    }

    public Integer generateRandomTimeout() {
        if(timeout.equals(timeoutScope)) {
            return timeout;
        }
        return RandomUtil.randomInt(timeout, timeoutScope);
    }

    public TimeUnit getTimeoutUnit() {
        return TimeUnit.MINUTES;
    }
}
