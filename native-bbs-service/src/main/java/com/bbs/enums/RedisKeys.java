package com.bbs.enums;

import cn.hutool.core.util.EnumUtil;
import lombok.Getter;
import java.util.Map;

/**
 * Redis 缓存 keys
 * <a href="https://developer.aliyun.com/article/531067">规范参考：阿里云</a>
 */
@Getter
public enum RedisKeys {

    NEW_THUMB_COMMENT("new_thumb_comment:", "文章id"),
    NEW_THUMB("new_thumb:", "文章点赞用户"),
    COMMENT("comment:", "评论用户"),
    COMMENT_THUMB("comment_thumb:", "评论点赞用户"),
    USER_NEW("user_new:", "用户发布的文章"),

    USER_THUMB("user_thumb:", "用户点赞数"),


    ;

    private final String prefix;

    private final String description;


    RedisKeys(String prefix, String description) {
        this.prefix = prefix;
        this.description = description;
    }

    private static final String LOCK_SUFFIX = "lock";

    public String key() {
        return prefix;
    }

    /**
     * key
     * @param mark 业务标识 / 表名 / 主键
     * @return key
     */
    public String key(String mark) {
        return prefix + ":" + mark;
    }

    @Override
    public String toString() {
        return key();
    }


    public static final Map<String, com.bbs.auth.enums.RedisKeys> map = EnumUtil.getEnumMap(com.bbs.auth.enums.RedisKeys.class);
}
