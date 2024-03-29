package com.bbs.content.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.content.dto.GetUserNewsDto;
import com.bbs.content.dto.param.CreateNewParam;
import com.bbs.content.service.CommentService;
import com.bbs.content.service.NewContentService;
import com.bbs.content.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
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

    private NewContentService newContentService;

    /**
     * 创建文章/视频
     * @param param param
     * @return Boolean
     */
    @PutMapping
    public Result<Boolean> createNews(@RequestBody @Valid CreateNewParam param){
        //TODO        UserVO currentUser = ThreadLocalUtil.getCurrentUser();
        param.setCreateId(1L);//currentUser.getid
        //创建文章表
        Long id = newsService.createNews(param);
        //创建文章text表
        newContentService.createByNew(id,param.getContent());
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
    public Result<Page<GetUserNewsDto>> getAccountNews(@NotNull Long userId,@NotNull Integer current, @NotNull Integer size,@NotNull Boolean flag){
        Page<GetUserNewsDto> newsResult = newsService.getListByUserId(userId,current,size,flag);
        return Result.success(newsResult);
    }


    /**
     * 查询推荐页上的内容简要信息
     * @param current 第几页
     * @param size 几条
     * @return Page<GetUserAccountDto.GetUserNewsDto>
     */
    @GetMapping("/recommend")
    public Result<Page<GetUserNewsDto>> getRecommendNews(@NotNull Integer current, @NotNull Integer size){
        Page<GetUserNewsDto> newsResult = newsService.getListByRecommend(current,size);
        return Result.success(newsResult);
    }


    /**
     * 查询关注页上的内容简要信息
     * @param userIds 用户id
     * @param current 第几页
     * @param size 几条
     * @return Page<GetUserAccountDto.GetUserNewsDto>
     */
    @GetMapping("/follower")
    public Result<Page<GetUserNewsDto>> getFollowerNews(@NotNull List<Long> userIds, @NotNull Integer current, @NotNull Integer size){
        Page<GetUserNewsDto> newsResult = newsService.getListByFollower(userIds,current,size);
        return Result.success(newsResult);
    }

    /**
     * 查询热门内容
     */
    //TODO



    /**
     *根据主键查全部内容、评论、点赞
     * @param newId 文章id
     * @return GetUserNewsDto
     */
    @GetMapping
    public Result<GetUserNewsDto> getOneById(@NotNull Long newId, @NotNull Integer current, @NotNull Integer size){
        GetUserNewsDto newsResult = newsService.getOneById(newId);
        if(Objects.nonNull(newsResult)){
            //获取评论分页列表
           Page<GetUserNewsDto.CommentByNewIdDto> list = commentService.getPageByNewId(newId, current, size);
            newsResult.setCommentByNewIdDtoList(list);
        }
        return Result.success(newsResult);
    }



    @Autowired
    public NewController(NewsService newsService, CommentService commentService, NewContentService newContentService) {
        this.newsService = newsService;
        this.commentService = commentService;
        this.newContentService = newContentService;
    }
}
