package com.bbs.file.controller;

import com.bbs.file.util.minio.FileOpt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping
public class Download {

    @Resource
    private FileOpt fileOpt;

    @GetMapping(value = "/download")
    public void download(@RequestParam String resourceId, HttpServletResponse response) {
        fileOpt.download(resourceId, response);
    }
}
