package com.bbs.content.api.content.upload;

import com.bbs.Result;
import com.bbs.api.DFS;
import com.bbs.content.api.content.cache.UploadFileCache;
import com.bbs.enums.dfs.BusinessCode;
import com.bbs.enums.dfs.FileType;
import com.bbs.enums.dfs.ResourceType;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

import static com.bbs.content.enums.RedisKeys.CONTENT_FILE_UPLOAD;

@RestController
@RequestMapping
public class Video {

    @Resource
    private DFS.Upload upload;

    @Resource
    private UploadFileCache uploadFileCache;

    @PostMapping("/upload/video")
    public Result<String> upload(
            @RequestParam String no,
            @RequestParam("file") MultipartFile file
    ) {
        DFS.Upload.VO vo = upload.uploadFile(
                BusinessCode.CONTENT_VIDEO,
                ResourceType.FILE,
                FileType.VIDEO,
                file
        );
        uploadFileCache.set(no, vo.getResourceID(), ResourceType.FILE, FileType.VIDEO);
        return Result.success(vo.getUrl());
    }
}
