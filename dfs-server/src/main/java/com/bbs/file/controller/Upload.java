package com.bbs.file.controller;

import com.bbs.Result;
import com.bbs.enums.dfs.FileType;
import com.bbs.enums.dfs.ResourceType;
import com.bbs.file.util.minio.FileOpt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@RestController
@RequestMapping
public class Upload {


    @Resource
    private FileOpt fileOpt;

    @PostMapping("/upload")
    public Result<String> upload(
            @RequestParam String businessCode,
            @RequestParam Integer resourceType,
            @RequestParam Integer fileType,
            @RequestParam("file") MultipartFile file,
            @RequestParam String contentType
    ) throws IllegalArgumentException {
        String id = fileOpt.resourceID(businessCode, resourceType, fileType);
        fileOpt.upload(id, file, contentType);
        return Result.success(fileOpt.preview(id));
    }
}
