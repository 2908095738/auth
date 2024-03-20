package com.bbs.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.dto.GetUserAccountDto;
import com.bbs.dto.GetUserNewsDto;
import com.bbs.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文章/视频
 */
@RestController
@RequestMapping("/new")
public class NewController {

    private NewsService newsService;

    /**
     *查询用户主页上的内容简要信息
     * @param userId
     * @return
     */
    @GetMapping
    public Result<Page<GetUserAccountDto.GetUserNewsDto>> getAccount(Long userId,Integer current, Integer size){
        Page<GetUserAccountDto.GetUserNewsDto> newsResult = newsService.getListByUserId(userId,current,size);
        return Result.success(newsResult);
    }

    /**
     *根据主键查全部内容、评论、点赞
     * @param newId
     * @return
     */
    @GetMapping
    public Result<GetUserNewsDto> getOneById(Long newId){
        GetUserNewsDto newsResult = newsService.getOneById(newId);
        return Result.success(newsResult);
    }



    @Autowired
    public NewController(NewsService newsService) {
        this.newsService = newsService;
    }
}
