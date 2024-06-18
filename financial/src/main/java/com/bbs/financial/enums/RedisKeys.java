package com.bbs.financial.enums;

import cn.hutool.core.util.EnumUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.Map;

/**
 * Redis 缓存 keys
 * <a href="https://developer.aliyun.com/article/531067">规范参考：阿里云</a>
 */
@Getter
public enum RedisKeys {

//
//    CONTENT_TMP_FILE_CLEAN("content:file:upload:tmp", "每日清除内容临时文件")
//    ;
//
//    private final String prefix;
//
//    private final String description;
//
//    public final Lock LOCK;
//
//
//    RedisKeys(String prefix, String description) {
//        this.prefix = prefix;
//        this.description = description;
//        this.LOCK = new Lock(prefix, description);
//    }
//
//    private static final String LOCK_SUFFIX = "lock";
//
//    public String key() {
//        return prefix;
//    }
//
//    /**
//     * key
//     * @param mark 业务标识 / 表名 / 主键
//     * @return key
//     */
//    public String key(String mark) {
//        return prefix + ":" + mark;
//    }
//
//    @Override
//    public String toString() {
//        return key();
//    }
//
//    @Data
//    @AllArgsConstructor
//    public static class Lock {
//
//        private final String prefix;
//
//        private final String description;
//
//        /**
//         * 锁 key
//         * @param mark 业务标识 / 表名 / 主键
//         * @return key
//         */
//        public String key(String mark) {
//            return prefix + mark + ":" + LOCK_SUFFIX;
//        }
//        public String key(Long mark) {
//            return key(mark.toString());
//        }
//
//        public String key(Integer mark) {
//            return key(mark.toString());
//        }
//
//        public String key() {
//            return prefix + ":" + LOCK_SUFFIX;
//        }
//
//        @Override
//        public String toString() {
//            return key();
//        }
//    }
//
//    public static final Map<String,RedisKeys> map = EnumUtil.getEnumMap(RedisKeys.class);

}
