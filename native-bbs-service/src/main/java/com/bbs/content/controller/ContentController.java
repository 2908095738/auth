package com.bbs.content.controller;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.content.cache.NewsCache;
import com.bbs.content.cache.ThumbCache;
import com.bbs.content.dto.GetContentDto;
import com.bbs.content.dto.GetUserNewsDto;
import com.bbs.content.dto.param.CreateNewParam;
import com.bbs.content.entity.News;
import com.bbs.content.service.CommentService;
import com.bbs.content.service.NewContentService;
import com.bbs.content.service.NewTagService;
import com.bbs.content.service.NewsService;
import com.bbs.content.service.TagService;
import com.bbs.content.util.AuthUtil;
import com.bbs.content.util.ThreadLocalUtil;
import com.bbs.content.util.TianDiTuUtil;
import com.bbs.vo.BaseParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.hutool.core.collection.CollUtil.isNotEmpty;

/**
 * 文章/视频
 */

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
    private TianDiTuUtil tianDiTu;


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


    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class QueryNewsParam extends BaseParam {
        /**
         * 查询类型
         */
        @NotNull
        private Integer queryType;

        /**
         *标题
         */
        private String title;

        /**
         * 标签ids
         */
        private List<Long> tagIds;

        /**
         * 定位所在城市
         */
        private String city;
        /**
         * 用户id
         */
        private Long userId;
        /**
         * 是否要查当前用户下发布的内容
         */
        private boolean userFlag = false;

        /**
         * 经度
         */
        private Double log;
        /**
         *维度
         */
        private Double lat;

    }


    /**
     * 条件查询内容
     * @param param  查询条件
     */
    @GetMapping("/query")
    public Result<Page<GetContentDto>> getQueryNews(@Valid QueryNewsParam param){
        Page<GetContentDto> result = new Page<>();
        switch (param.getQueryType()) {
            case 1:
                //关注页：
                //TODO 根据用户id查询当前用户关注的列表 ThreadLocalUtil.getCurrentUserId()
                List<Long> userIds = new ArrayList<>();
                result = newsService.getListByFollower(param.getCurrent(),param.getSize(),userIds,param.title);
                break;
            case 2:
                //推荐页：
                result = newsService.getListByRecommend(param.getCurrent(),param.getSize());
                break;
            case 3:
                //本地页：
                String city = tianDiTu.getCityBy(param.getLog(),param.getLat());
                result = newsService.getListByNative(param.getCurrent(),param.getSize(),city, param.title);
                break;
            case 4:
                //用户(自己或他人)主页：
                result = newsService.getListByUserId(param.getCurrent(),param.getSize(),param.userId,param.userFlag,param.title);
                break;
        }
        if(isNotEmpty(result.getRecords())) {
            List<GetContentDto> list = result.getRecords();
            Set<Long> userIds = list.stream().map(GetContentDto::getCreateId).collect(Collectors.toSet());
            Map<Long, AuthUtil.UserAPI.VO> userIdMap = new HashMap<>();
            if(CollUtil.isNotEmpty(userIds)&&userIds.size()>1){
                userIdMap = api.getUserList(new ArrayList<>(userIds)).stream().collect(Collectors.toMap(AuthUtil.UserAPI.VO::getId, o2 -> o2));
            }{
                AuthUtil.UserAPI.VO userByid = api.getUserByid(new ArrayList<>(userIds).get(0));
                userIdMap.put(userByid.getId(),userByid);
            }
            Map<Long, Integer> visitMap = newsCache.getVisit(list.stream().map(GetContentDto::getNewId).collect(Collectors.toList()));
            for (GetContentDto getContentDto : result.getRecords()) {
                getContentDto.setUser(userIdMap.get(getContentDto.getCreateId()));
                getContentDto.setLikeCount((Integer) thumbCache.countBy(getContentDto.getNewId(), null, null, 1));
                getContentDto.setVisitNum(visitMap.getOrDefault(getContentDto.getNewId(), 0));
            }
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
     * 删除内容
     */
    @DeleteMapping()
    public Result<Boolean> deleteNews(@NotNull(message = "内容id不能为空！") Long newId){
        AuthUtil.UserAPI.User currentUser = ThreadLocalUtil.getCurrentUser();
        newsService.delete(newId,currentUser.getId());
        return Result.success();
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
            Page<GetUserNewsDto.CommentByNewIdDto> commentPage = commentService.getPageByNewId(newId,null, current, size);
            if(isNotEmpty(commentPage.getRecords())) {
                List<Long> commentIds = new ArrayList<>();
                Set<Long> userIds = new HashSet<>();
                commentPage.getRecords().forEach(comment->{
                    userIds.add(comment.getCreateId());
                    commentIds.add(comment.getId());
                });

                Map<Long, AuthUtil.UserAPI.VO> userIdMap = new HashMap<>();
                if(CollUtil.isNotEmpty(userIds)&&userIds.size()>1){
                    userIdMap = api.getUserList(new ArrayList<>(userIds)).stream().collect(Collectors.toMap(AuthUtil.UserAPI.VO::getId, o2 -> o2));
                }{
                    AuthUtil.UserAPI.VO commentUser = api.getUserByid(new ArrayList<>(userIds).get(0));
                    userIdMap.put(commentUser.getId(),commentUser);
                }
                Map<Long, Set<Long>> commentThumbUsersMap = (Map<Long, Set<Long>>) thumbCache.countBy(newId, null, commentIds, 3);


                for(GetUserNewsDto.CommentByNewIdDto comment : commentPage.getRecords()){
                    AuthUtil.UserAPI.VO vo = userIdMap.get(comment.getCreateId());
                    Set<Long> thumbUserIds = commentThumbUsersMap.get(comment.getId());
                    comment.setAvatar(vo.getAvatar());//头像
                    comment.setNickName(vo.getName());//名字
                    comment.setHasLike(thumbUserIds.contains(currentUserId));//是否点赞
                    comment.setAllowDelete(Objects.equals(comment.getCreateId(), currentUserId));//是否可以删除此评论（自己评论或管理员）
                    comment.setLikeNum(thumbUserIds.size());//点赞数
                }
            }

            AuthUtil.UserAPI.VO userByid = api.getUserByid(result.getCreateId());
            result.setUser(userByid);
            result.setLikeCount((Integer) thumbCache.countBy(result.getNewId(), null, null, 1));

            result.setCommentByNewIdDtoList(commentPage);
            result.setThisUser(Objects.equals(currentUserId, result.getCreateId()));//是否是当前用户
            //访问量加1
            Integer visitNum = newsCache.intrVisit(newId);
            result.setVisitNum(visitNum);
        }
        return Result.success(result);
    }



    @Autowired
    public ContentController(NewsService newsService, NewsCache newsCache, ThumbCache thumbCache, CommentService commentService, NewContentService newContentService, NewTagService newTagService, TagService tagService, TransactionDefinition transactionDefinition, DataSourceTransactionManager transactionManager, AuthUtil.UserAPI api, TianDiTuUtil tianDiTu) {
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
        this.tianDiTu = tianDiTu;
    }
}
