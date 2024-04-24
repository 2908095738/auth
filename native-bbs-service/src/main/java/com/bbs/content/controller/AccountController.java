package com.bbs.content.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.content.cache.ThumbCache;
import com.bbs.content.dto.GetContentDto;
import com.bbs.content.dto.GetUserAccountDto;
import com.bbs.content.service.NewsService;
import com.bbs.content.service.UserAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

import static cn.hutool.core.collection.CollUtil.isNotEmpty;

/**
 *用户账号管理
 */
@RestController
@RequestMapping("/account")
public class AccountController {

    private UserAccountService service;

    private NewsService newsService;

    private ThumbCache thumbCache;



    /**
     * 查看登录用户发布内容列表
     * 头像、昵称、性别、点赞数、
     * 发布内容列表  文章or视频or物品 ：标题、内容概要、评论数、收藏数、点赞数
     */
    @GetMapping()
    public Result<GetUserAccountDto> getAccount(Long userId, boolean flag){
        //获取用户账号信息
        GetUserAccountDto result = service.getByUserId(userId);
        if(Objects.nonNull(result)){
            //获取发布文章列表
            Page<GetContentDto> newsResult = newsService.getListByUserId(1,10,userId, 1, flag,null);
            if(isNotEmpty(newsResult.getRecords()))
                //点赞数
                newsResult.getRecords().forEach(o -> o.setLikeCount((Integer) thumbCache.countBy(o.getNewId(), null, null, 1)));
                //收藏记录
                //浏览记录

            result.setNewsResult(newsResult);
        }
        return Result.success(result);
    }


    @Autowired
    public AccountController(UserAccountService service, NewsService newsService, ThumbCache thumbCache) {
        this.service = service;
        this.newsService = newsService;
        this.thumbCache = thumbCache;
    }
}
