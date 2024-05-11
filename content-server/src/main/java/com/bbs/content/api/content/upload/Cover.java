package com.bbs.content.api.content.upload;

import com.bbs.Result;
import com.bbs.api.DFS;
import com.bbs.content.api.content.cache.UploadFileCache;
import com.bbs.enums.dfs.BusinessCode;
import com.bbs.enums.dfs.FileType;
import com.bbs.enums.dfs.ResourceType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@RestController
@RequestMapping
public class Cover {

    @Resource
    private DFS.Upload upload;

    @Resource
    private UploadFileCache uploadFileCache;

    @PostMapping("/upload/cover")
    public Result<String> upload(
            @RequestParam String no,
            @RequestParam("file") MultipartFile file
    ) {
        DFS.Upload.VO vo = upload.uploadFile(
                BusinessCode.CONTENT_COVER,
                ResourceType.FILE,
                FileType.IMAGE,
                file
        );
        uploadFileCache.set(no, vo.getResourceID(), ResourceType.FILE, FileType.IMAGE);
        return Result.success(vo.getUrl());
    }
}
