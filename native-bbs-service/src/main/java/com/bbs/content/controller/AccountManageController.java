package com.bbs.content.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.content.dto.GetUserAccountDto;
import com.bbs.content.dto.GetUserNewsDto;
import com.bbs.content.service.NewsService;
import com.bbs.content.service.UserAccountService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Objects;

/**
 *用户账号管理
 */
@RestController
@RequestMapping("/account")
public class AccountManageController {

    private UserAccountService service;

    private NewsService newsService;

    /**
     * 创建登录用户账号信息
     * 头像、昵称、性别、年龄、点赞数、积分数、收藏数、关注数、粉丝数、是否企业认证、是否实名认证
     */
    @PutMapping()
    public Result<Boolean> createAccount(Long userId){
        //获取用户账号信息
        service.create(userId);
        return Result.success();
    }



    /**
     * 查看登录用户账号信息+发布内容列表
     * 头像、昵称、性别、年龄；点赞数、积分数、收藏数、关注数、粉丝数、(粉丝账户id列表\关注账户id列表\收藏文章id列表)是否企业认证、是否实名认证
     * 发布内容列表  文章or视频1：标题、内容概要、评论数、收藏数、点赞数
     */
    @GetMapping()
    public Result<GetUserAccountDto> getAccount(Long userId){
//TODO        UserVO currentUser = ThreadLocalUtil.getCurrentUser();
        //获取用户账号信息
        GetUserAccountDto result = service.getByUserId(userId);
        if(Objects.nonNull(result)){
            result.setAge(18);//TODO currentUser
            //获取发布文章列表
            Page<GetUserNewsDto> newsResult = newsService.getListByUserId(userId,1,10,true);
            result.setNewsResult(newsResult);
        }
        return Result.success(result);
    }


    @Resource
    public void setService(UserAccountService service) {
        this.service = service;
    }
    @Resource
    public void setNewsService(NewsService newsService) {
        this.newsService = newsService;
    }
}
