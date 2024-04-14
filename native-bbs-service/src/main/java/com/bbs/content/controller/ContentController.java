package com.bbs.content.controller;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.content.cache.NewsCache;
import com.bbs.content.cache.ThumbCache;
import com.bbs.content.dto.GetContentDto;
import com.bbs.content.dto.GetUserNewsDto;
import com.bbs.content.dto.param.CreateNewParam;
import com.bbs.content.dto.param.QueryNewsParam;
import com.bbs.content.entity.News;
import com.bbs.content.service.CommentService;
import com.bbs.content.service.NewContentService;
import com.bbs.content.service.NewTagService;
import com.bbs.content.service.NewsService;
import com.bbs.content.service.TagService;
import com.bbs.content.util.AuthUtil;
import com.bbs.content.util.ThreadLocalUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.hutool.core.collection.CollUtil.isNotEmpty;

/**
 * 文章/视频
 */

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/content")
public class ContentController {

    private NewsService newsService;
    private NewsCache newsCache;
    private ThumbCache thumbCache;
    private CommentService commentService;
    private NewContentService newContentService;
    private NewTagService newTagService;

    private TagService tagService;
    private TransactionDefinition transactionDefinition;
    private DataSourceTransactionManager transactionManager;
    private AuthUtil.UserAPI api;


    /**
     * 获取内容id：创建id
     *
     * @return Long
     */
    @GetMapping("/id")
    public Result<Long> getNewsId() {
        AuthUtil.UserAPI.User currentUser = ThreadLocalUtil.getCurrentUser();
        Long id = newsService.createNewsId(currentUser.getId(), currentUser.getName());
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
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            News news = newsService.createNews(param);
            if(StringUtils.isNotBlank(param.getContent()))newContentService.createByNew(param.getNewId(), param.getContent());
            if(CollUtil.isNotEmpty(param.getTagIds())){
                List<Long> newIds = tagService.addAndUpdateWeight(param.getTagNames(), param.getTagIds());
                param.getTagIds().addAll(newIds);
                newTagService.createByNew(param.getNewId(), param.getTagIds());
            }
            newsCache.create(news.getNewId(),param);
            transactionManager.commit(transaction);
            return Result.success();
        } catch (RuntimeException e) {
            transactionManager.rollback(transaction);
            e.printStackTrace();
        }
        return Result.failedNull();
    }


    /**
     * 条件查询内容
     */
    @GetMapping("/query")
    public Result<Page<GetContentDto>> getQueryNews(@Valid QueryNewsParam param){
        Page<GetContentDto> result = newsService.getListByQuery(param);
        if(isNotEmpty(result.getRecords())) {
            Map<Long, AuthUtil.UserAPI.VO> collect = api.getUserList(result.getRecords().stream().map(GetContentDto::getCreateId).collect(Collectors.toList())).stream().collect(Collectors.toMap(o1 -> o1.getId(), o2 -> o2));
            result.getRecords().forEach(o ->{
                o.setLikeCount(thumbCache.countBy(o.getNewId(), null, null, 1));
                o.setUser(collect.get(o.getCreateId()));
            });
        }
        return Result.success(result);
    }


    /**
     * 删除内容
     */
    @DeleteMapping()
    public Result<Boolean> deleteNews(@NotNull(message = "内容id不能为空！") Long newId){
        AuthUtil.UserAPI.User currentUser = ThreadLocalUtil.getCurrentUser();
        newsService.delete(newId,currentUser.getId());
        return Result.success();
    }


    /**
     * 查询关注页上的内容简要信息
     * @param current 第几页
     * @param size    几条
     * @return Page<GetUserAccountDto.GetUserNewsDto>
     */
    @GetMapping("/follower")
    public Result<Page<GetContentDto>> getFollowerNews(@NotNull(message = "页数不能为空！") Integer current,
                                                        @NotNull(message = "每页几条不能为空！") Integer size) {
        Page<GetContentDto> result = newsService.getListByFollower(ThreadLocalUtil.getCurrentUser().getId(), current, size);
        if(isNotEmpty(result.getRecords())){
            Map<Long, AuthUtil.UserAPI.VO> collect = api.getUserList(result.getRecords().stream().map(GetContentDto::getCreateId).collect(Collectors.toList())).stream().collect(Collectors.toMap(o1 -> o1.getId(), o2 -> o2));
            result.getRecords().forEach(o ->{
                o.setLikeCount(thumbCache.countBy(o.getNewId(), null, null, 1));
                o.setUser(collect.get(o.getCreateId()));
            });
        }
        return Result.success(result);
    }



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
    public Result<Page<GetContentDto>> getAccountNews(@NotNull(message = "用户id不能为空！") Long userId,
                                                       @NotNull(message = "页数不能为空！") Integer current,
                                                       @NotNull(message = "每页几条不能为空！") Integer size,
                                                       @NotNull(message = "是否为此用户属性值不能为空！") Boolean flag) {
        Page<GetContentDto> newsResult = newsService.getListByUserId(userId, current, size, flag);
        if(isNotEmpty(newsResult.getRecords())){
            Map<Long, AuthUtil.UserAPI.VO> collect = api.getUserList(newsResult.getRecords().stream().map(GetContentDto::getCreateId).collect(Collectors.toList())).stream().collect(Collectors.toMap(o1 -> o1.getId(), o2 -> o2));
            newsResult.getRecords().forEach(o ->{
                o.setLikeCount(thumbCache.countBy(o.getNewId(), null, null, 1));
                o.setUser(collect.get(o.getCreateId()));
            });
        }
            newsResult.getRecords().forEach(o -> o.setLikeCount(thumbCache.countBy(o.getNewId(), null, null, 1)));
        return Result.success(newsResult);
    }



    /**
     * 查询推荐页上的内容简要信息
     * @return Page<GetUserAccountDto.GetUserNewsDto>
     */
    @GetMapping("/recommend")
    public Result<List<GetContentDto>> getRecommendNews() {
        List<GetContentDto> result = newsService.getListByRecommend();
        if(isNotEmpty(result)) {
            Set<Long> userIds = result.stream().map(GetContentDto::getCreateId).collect(Collectors.toSet());

            Map<Long, AuthUtil.UserAPI.VO> userIdMap = new HashMap<>();
            if(CollUtil.isNotEmpty(userIds)&&userIds.size()>1){
                userIdMap = api.getUserList(new ArrayList<>(userIds)).stream().collect(Collectors.toMap(AuthUtil.UserAPI.VO::getId, o2 -> o2));
            }{
                AuthUtil.UserAPI.VO userByid = api.getUserByid(new ArrayList<>(userIds).get(0));
                userIdMap.put(userByid.getId(),userByid);
            }
            Map<Long, Integer> visitMap = newsCache.getVisit(result.stream().map(GetContentDto::getNewId).collect(Collectors.toList()));
            Map<Long, AuthUtil.UserAPI.VO> finalUserIdMap = userIdMap;
            result.forEach(o -> {
                o.setUser(finalUserIdMap.get(o.getCreateId()));
                o.setLikeCount(thumbCache.countBy(o.getNewId(), null, null, 1));
                o.setVisitNum(visitMap.getOrDefault(o.getNewId(), 0));
            });
        }
        return Result.success(result);
    }



    /**
     * 查询热门内容
     */
    @GetMapping("/hot")
    public Result<List<GetContentDto>> getHotNews() {
        //TODO
        List<GetContentDto> newsResult = newsCache.getHot();
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
                                             @NotNull(message = "评论页数不能为空！") Integer current,
                                             @NotNull(message = "评论每页几条不能为空！") Integer size) {
        Long currentUserId = ThreadLocalUtil.getCurrentUserId();
        GetUserNewsDto result = newsService.getOneById(newId);
        if (Objects.nonNull(result)) {
            AuthUtil.UserAPI.VO userByid = api.getUserByid(result.getCreateId());
            result.setUser(userByid);
            result.setLikeCount(thumbCache.countBy(result.getNewId(), null, null, 1));
            Page<GetUserNewsDto.CommentByNewIdDto> list = commentService.getPageByNewId(newId, current, size);
            result.setCommentByNewIdDtoList(list);
            result.setThisUser(Objects.equals(currentUserId, result.getCreateId()));
            //访问量加1
            Integer visitNum = newsCache.intrVisit(newId);
            result.setVisitNum(visitNum);
        }
        return Result.success(result);
    }


    @Autowired
    public ContentController(NewsService newsService, NewsCache newsCache, ThumbCache thumbCache, CommentService commentService, NewContentService newContentService, NewTagService newTagService, TagService tagService, TransactionDefinition transactionDefinition, DataSourceTransactionManager transactionManager, AuthUtil.UserAPI api) {
        this.newsService = newsService;
        this.newsCache = newsCache;
        this.thumbCache = thumbCache;
        this.commentService = commentService;
        this.newContentService = newContentService;
        this.newTagService = newTagService;
        this.tagService = tagService;
        this.transactionDefinition = transactionDefinition;
        this.transactionManager = transactionManager;
        this.api = api;
    }
}
