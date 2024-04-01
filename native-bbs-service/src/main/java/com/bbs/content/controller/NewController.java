package com.bbs.content.controller;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.content.cache.NewsCache;
import com.bbs.content.dto.GetUserNewsDto;
import com.bbs.content.dto.param.CreateNewParam;
import com.bbs.content.entity.News;
import com.bbs.content.service.CommentService;
import com.bbs.content.service.NewContentService;
import com.bbs.content.service.NewTagService;
import com.bbs.content.service.NewsService;
import org.apache.commons.lang3.StringUtils;
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

    private NewsCache newsCache;

    private CommentService commentService;

    private NewContentService newContentService;

    private NewTagService newTagService;

    /**
     * 获取内容id：创建id
     *
     * @return Long
     */
    @GetMapping("/id")
    public Result<Long> getNewsId() {
        //TODO        UserVO currentUser = ThreadLocalUtil.getCurrentUser();
        String userName = "testName";
        Long createId = 1L;
        Long id = newsService.createNewsId(createId, userName);
        return Result.success(id);
    }


    /**
     * 创建文章/视频
     *
     * @param param param
     * @return Boolean
     */
    @PutMapping
    public Result<Boolean> createNews(@RequestBody @Valid CreateNewParam param) {
        //创建文章表
        News news = newsService.createNews(param);
        //创建文章text表
        if(StringUtils.isNotBlank(param.getContent()))newContentService.createByNew(param.getNewId(), param.getContent());
        //添加标签ids
        if(CollUtil.isNotEmpty(param.getTagIds()))newTagService.createByNew(param.getNewId(), param.getTagIds());
        // 计算内容分数

        //添加到redis
        newsCache.create(news);
        return Result.success();
    }

    /**
     * 删除草稿
     */




    /**
     * 删除内容
     */







    /**
     * 查询用户(自己或他人)主页上的内容简要信息
     *
     * @param userId  用户id
     * @param current 第几页
     * @param size    几条
     * @param flag    是否是用户自己  true：是
     * @return Page<GetUserAccountDto.GetUserNewsDto>
     */
    @GetMapping("/user")
    public Result<Page<GetUserNewsDto>> getAccountNews(@NotNull(message = "用户id不能为空！") Long userId,
                                                       @NotNull(message = "页数不能为空！") Integer current,
                                                       @NotNull(message = "每页几条不能为空！") Integer size,
                                                       @NotNull(message = "是否为此用户属性值不能为空！") Boolean flag) {
        Page<GetUserNewsDto> newsResult = newsService.getListByUserId(userId, current, size, flag);
        return Result.success(newsResult);
    }


    /**
     * 查询推荐页上的内容简要信息
     *
     * @param current 第几页
     * @param size    几条
     * @return Page<GetUserAccountDto.GetUserNewsDto>
     */
    @GetMapping("/recommend")
    public Result<Page<GetUserNewsDto>> getRecommendNews(@NotNull(message = "页数不能为空！") Integer current,
                                                         @NotNull(message = "每页几条不能为空！") Integer size) {
        Page<GetUserNewsDto> newsResult = newsService.getListByRecommend(current, size);
        return Result.success(newsResult);
    }


    /**
     * 查询关注页上的内容简要信息
     *
     * @param userIds 用户id
     * @param current 第几页
     * @param size    几条
     * @return Page<GetUserAccountDto.GetUserNewsDto>
     */
    @GetMapping("/follower")
    public Result<Page<GetUserNewsDto>> getFollowerNews(@NotNull(message = "关注用户id列表不能为空！") List<Long> userIds,
                                                        @NotNull(message = "页数不能为空！") Integer current,
                                                        @NotNull(message = "每页几条不能为空！") Integer size) {
        Page<GetUserNewsDto> newsResult = newsService.getListByFollower(userIds, current, size);
        return Result.success(newsResult);
    }

    /**
     * 查询热门内容
     */
    @GetMapping("/hot")
    public Result<List<GetUserNewsDto>> getHotNews() {
        //TODO
        List<GetUserNewsDto> newsResult = newsCache.getHot();
        return Result.success(newsResult);
    }


    /**
     * 根据主键查全部内容、评论、点赞
     *
     * @param newId 文章id
     * @return GetUserNewsDto
     */
    @GetMapping
    public Result<GetUserNewsDto> getOneById(@NotNull(message = "内容id不能为空！") Long newId,
                                             @NotNull(message = "页数不能为空！") Integer current,
                                             @NotNull(message = "每页几条不能为空！") Integer size) {
        GetUserNewsDto newsResult = newsService.getOneById(newId);
        if (Objects.nonNull(newsResult)) {
            //获取评论分页列表
            Page<GetUserNewsDto.CommentByNewIdDto> list = commentService.getPageByNewId(newId, current, size);
            newsResult.setCommentByNewIdDtoList(list);
        }
        return Result.success(newsResult);
    }


    @Autowired
    public NewController(NewsService newsService, NewsCache newsCache, CommentService commentService, NewContentService newContentService, NewTagService newTagService) {
        this.newsService = newsService;
        this.newsCache = newsCache;
        this.commentService = commentService;
        this.newContentService = newContentService;
        this.newTagService = newTagService;
    }
}
