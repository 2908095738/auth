package com.bbs.file.controller;

import com.bbs.Result;
import com.bbs.file.util.minio.FileOpt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
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
    ) {
        try {
        String resourceID = fileOpt.resourceID(businessCode, resourceType, fileType);
        return Result.success(fileOpt.upload(resourceID, file, contentType));
    } catch (Exception e) {
        log.error(e.getMessage(), e);
        e.printStackTrace();
        return null;
    }
    }

    @DeleteMapping("/batch")
    public Result<Boolean> deleteList(@RequestParam("resourceIds") List<String> resourceIds){
        return Result.success(fileOpt.removeList(resourceIds));
    }



}
