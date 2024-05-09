package com.bbs.content.enums;

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

    NEW_THUMB_COMMENT("new_thumb_comment:", "内容id"),
    NEW_THUMB("new_thumb:", "内容点赞用户"),
    COMMENT("comment:", "评论用户"),
    COMMENT_THUMB("comment_thumb:", "评论点赞用户"),
    USER_NEW("user_new:", "用户发布的内容"),

    USER_THUMB("user_thumb:", "用户点赞数"),

    HOT_NEWS("hot_news:", "热点内容"),

    AUDIT_USERID_NEWS("audit_userid_news:", "用户的待审核内容"),
    AUDIT_NEW_FIlE("audit_new_f:", "用户的待审核图片or视频"),
    NEW("NEW_id_", "内容id"),
    AUDIT_FILE_SIZE("a_f_size:", "待审核内容中的文件数量"),
    CONTENT_VISIT_NUN_INCR("content_visit_nun:incr:", "发布内容访问量"),

    CONTENT("content:", "内容"),

    CONTENT_FILE_UPLOAD("content:file:upload", "内容文件上传"),
    CONTENT_FILE_UPLOAD_TMP("content:file:upload:tmp", "内容临时文件上传"),
    CONTENT_TMP_FILE_CLEAN("content:file:upload:tmp", "每日清除内容临时文件")
    ;

    private final String prefix;

    private final String description;

    public final Lock LOCK;


    RedisKeys(String prefix, String description) {
        this.prefix = prefix;
        this.description = description;
        this.LOCK = new Lock(prefix, description);
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

    @Data
    @AllArgsConstructor
    public static class Lock {

        private final String prefix;

        private final String description;

        /**
         * 锁 key
         * @param mark 业务标识 / 表名 / 主键
         * @return key
         */
        public String key(String mark) {
            return prefix + mark + ":" + LOCK_SUFFIX;
        }
        public String key(Long mark) {
            return key(mark.toString());
        }

        public String key(Integer mark) {
            return key(mark.toString());
        }

        public String key() {
            return prefix + ":" + LOCK_SUFFIX;
        }

        @Override
        public String toString() {
            return key();
        }
    }

    public static final Map<String,RedisKeys> map = EnumUtil.getEnumMap(RedisKeys.class);

}
