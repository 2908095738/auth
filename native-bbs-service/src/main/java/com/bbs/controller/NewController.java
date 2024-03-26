package com.bbs.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.dto.GetUserAccountDto;
import com.bbs.dto.GetUserNewsDto;
import com.bbs.dto.param.CreateNewParam;
import com.bbs.service.CommentService;
import com.bbs.service.NewsService;
import com.bbs.util.IpConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;

/**
 * 文章/视频
 */
@RestController
@RequestMapping("/news")
public class NewController {

    private NewsService newsService;

    private CommentService commentService;


    /**
     * 创建文章/视频
     * @param
     * @return
     */
    @PutMapping
    public Result createNews(CreateNewParam param, HttpServletRequest request){
        //TODO        UserVO currentUser = ThreadLocalUtil.getCurrentUser();
        param.setCreateId(1L);//currentUser.getid
        String ip = IpConfig.getIpAdrress(request);//获取ip
        param.setIp(ip);
        //获取url

        //创建文章表
        newsService.createNews(param);
        //创建文章text表

        // 计算内容分数

        return Result.success();
    }



    /**
     * 查询用户(自己或他人)主页上的内容简要信息
     * @param userId 用户id
     * @param current 第几页
     * @param size 几条
     * @return Page<GetUserAccountDto.GetUserNewsDto>
     */
    @GetMapping("/user")
    public Result<Page<GetUserAccountDto.GetUserNewsDto>> getAccountNews(Long userId,Integer current, Integer size){
        Page<GetUserAccountDto.GetUserNewsDto> newsResult = newsService.getListByUserId(userId,current,size);
        return Result.success(newsResult);
    }


    /**
     * 查询推荐页上的内容简要信息
     * @param current 第几页
     * @param size 几条
     * @return
     */
    @GetMapping("/recommend")
    public Result<Page<GetUserAccountDto.GetUserNewsDto>> getRecommendNews(Integer current, Integer size){
        Page<GetUserAccountDto.GetUserNewsDto> newsResult = newsService.getListByRecommend(current,size);
        return Result.success(newsResult);
    }


    /**
     * 查询关注页上的内容简要信息
     * @param userIds 用户id
     * @param current 第几页
     * @param size 几条
     * @return
     */
    @GetMapping("/follower")
    public Result<Page<GetUserAccountDto.GetUserNewsDto>> getFollowerNews(List<Long> userIds, Integer current, Integer size){
        Page<GetUserAccountDto.GetUserNewsDto> newsResult = newsService.getListByFollower(userIds,current,size);
        return Result.success(newsResult);
    }



    /**
     *根据主键查全部内容、评论、点赞
     * @param newId 文章id
     * @return
     */
    @GetMapping
    public Result<GetUserNewsDto> getOneById(Long newId, Integer current, Integer size){
        GetUserNewsDto newsResult = newsService.getOneById(newId);
        if(Objects.nonNull(newsResult)){
            //获取评论分页列表
           Page<GetUserNewsDto.CommentByNewIdDto> list = commentService.getPageByNewId(newId, current, size);
            newsResult.setCommentByNewIdDtoList(list);
        }
        return Result.success(newsResult);
    }



    @Autowired
    public NewController(NewsService newsService, CommentService commentService) {
        this.newsService = newsService;
        this.commentService = commentService;
    }
}
