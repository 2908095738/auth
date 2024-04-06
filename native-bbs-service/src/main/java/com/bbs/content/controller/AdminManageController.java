package com.bbs.content.controller;


import com.bbs.Result;
import com.bbs.content.cache.FileCache;
import com.bbs.content.dto.AuditNewDto;
import com.bbs.content.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminManageController {

    private NewsService newsService;

    private FileCache fileCache;




    /**
     * 查询待审核内容+视频+图片
     */
    @GetMapping("/new")
    public Result<List<AuditNewDto>> getWaitAudit(){
        return Result.success(fileCache.getAuditFile());
    }


    /**
     * 审核通过
     */


    /**
     * 审核不通过：原因
     */


    /**
     * 删除评论
     */








    @Autowired
    public AdminManageController(NewsService newsService, FileCache fileCache) {
        this.newsService = newsService;
        this.fileCache = fileCache;
    }
}
