package com.bbs.file.controller;

import com.bbs.Result;
import com.bbs.api.DFS;
import com.bbs.enums.dfs.FileType;
import com.bbs.file.util.minio.FileOpt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping
public class TemporaryUpload {

    @Resource
    private FileOpt fileOpt;
    @PostMapping("/upload/temporary")
    public Result<DFS.Upload.VO> upload(
            @RequestParam String businessCode,
            @RequestParam Integer resourceType,
            @RequestParam Integer fileType,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            String resourceID = fileOpt.resourceID(businessCode, resourceType, fileType);
            String url = fileOpt.uploadTheSameDayBucket(resourceID, file, FileType.map.get(resourceType).getContentType());
            return Result.success(new DFS.Upload.VO(url, resourceID));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            e.printStackTrace();
            return null;
        }
    }
}
