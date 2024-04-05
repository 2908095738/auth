package com.bbs.file.cache;

import com.bbs.file.util.RedisUtil;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.bbs.file.enums.RedisKeys.FILE_PATH;

/**
 * 文件缓存（代表具体文件，非目录）
 * 结构：<ResourceID, FilePath>
 */
@Component
public class FilePathCache {

    @Resource
    private RedisUtil redis;

    public String get(String resourceID) {
        return redis.get(FILE_PATH.key(resourceID));
    }

    public void set(String resourceID, String filePath) {
        redis.set(FILE_PATH.key(resourceID), filePath);
    }

    public void del(String resourceID) {
        redis.delete(FILE_PATH.key(resourceID));
    }

    public Boolean exists(String resourceID) {
        return redis.exists(FILE_PATH.key(resourceID));
    }

    public Boolean notExists(String resourceID) {
        return !exists(FILE_PATH.key(resourceID));
    }
}
