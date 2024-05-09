package com.bbs.content.api.content.cache;

import cn.hutool.json.JSONUtil;
import com.bbs.enums.dfs.FileType;
import com.bbs.enums.dfs.ResourceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.bbs.content.enums.RedisKeys.CONTENT_FILE_UPLOAD;

@Slf4j
@Component
public class UploadFileCache {

    @Resource(name = "protoStuffTemplate")
    private RedisTemplate<String, String> protoStuffTemplate;

    public String key(String no) {
        return CONTENT_FILE_UPLOAD.key(no);
    }

    public void set(String no, String resourceID, ResourceType resourceType, FileType fileType) {
        File file = new File(no, resourceID, resourceType, fileType);
        protoStuffTemplate.opsForSet().add(CONTENT_FILE_UPLOAD.key(no), JSONUtil.toJsonPrettyStr(file));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class File {

        private String contentNO;

        private String resourceID;

        private ResourceType resourceType;

        private FileType fileType;
    }
}
