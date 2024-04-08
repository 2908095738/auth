package com.bbs.file.controller;

import com.bbs.Result;
import com.bbs.file.service.FileService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@RestController
@RequestMapping
public class Upload {

    @Resource
    private FileService service;

    /**
     * 文件上传
     * @param resourceID  资源编号（全局唯一）
     * @param file 文件
     * @return 上传结果
     * @throws IllegalArgumentException 资源 ID不可用
     */
    @PostMapping
    public Result<Boolean> file(String resourceID, MultipartFile file) throws IllegalArgumentException {
        service.upload(resourceID, file);
        return Result.success();
    }
}
