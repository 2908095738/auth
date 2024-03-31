package com.bbs.content.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.content.cache.ThumbCache;
import com.bbs.content.converter.NewsConverter;
import com.bbs.content.dto.GetUserNewsDto;
import com.bbs.content.dto.param.CreateNewParam;
import com.bbs.content.entity.NewContent;
import com.bbs.content.entity.News;
import com.bbs.content.enums.NewCommentStatus;
import com.bbs.content.mapper.NewsMapper;
import com.bbs.content.service.NewsService;
import com.bbs.content.util.SensitiveFilter;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

import static cn.hutool.core.collection.CollUtil.isNotEmpty;

/**
 *
 */
@Service
public class NewsServiceImpl extends MPJBaseServiceImpl<NewsMapper, News>
    implements NewsService{

    private NewsConverter converter;

    private SensitiveFilter sensitiveFilter;

    private ThumbCache thumbCache;

    /**
     * 创建文章或视频
     * @param param 文章或视频
     * @return Long
     */
    @Override
    public News createNews(CreateNewParam param) {
       News news = converter.toEntity(param);
       // 转义 HTML 标记，防止在 HTML 标签中注入攻击语句
        news.setTitle(HtmlUtils.htmlEscape(news.getTitle()));
        // 过滤敏感词
        news.setTitle(sensitiveFilter.filter(news.getTitle()));
        news.setStatus(NewCommentStatus.WAIT_FOR_REVIEW.getCode());
        news.setUpdateId(news.getCreateId());
        save(news);
        return news;
    }

    /**
     * 查询用户主页上发布内容集合
     * @param userId 用户id
     * @param current 第几页
     * @param size 几条
     * @param flag 是否是用户自己  true：是
     * @return GetUserAccountDto.GetUserNewsDto
     */
    @Override
    public Page<GetUserNewsDto> getListByUserId(Long userId, Integer current, Integer size, boolean flag) {
        Page<GetUserNewsDto> result = selectJoinListPage(new Page<>(current, size), GetUserNewsDto.class, new MPJLambdaWrapper<News>()
                .selectAll(News.class)
                .orderBy(true,false,News::getCreateTime)
                .eq(News::getCreateId,userId)
                .in(flag,News::getStatus, NewCommentStatus.HAVE_RELEASED.getCode(), NewCommentStatus.WAIT_FOR_REVIEW.getCode())
                .eq(!flag,News::getStatus, NewCommentStatus.HAVE_RELEASED.getCode())
        );
        if(isNotEmpty(result.getRecords()))
            result.getRecords().forEach(o -> o.setLikeCount(thumbCache.countBy(o.getNewId(), null, null, 1)));
        return result;
    }


    /**
     * 查询推荐页上的内容简要信息
     * @param current 第几页
     * @param size 几条
     * @return GetUserAccountDto.GetUserNewsDto
     */
    @Override
    public Page<GetUserNewsDto> getListByRecommend(Integer current, Integer size) {
        Page<GetUserNewsDto> result = selectJoinListPage(new Page<>(current, size), GetUserNewsDto.class, new MPJLambdaWrapper<News>()
                .selectAll(News.class)
                .eq(News::getStatus, NewCommentStatus.HAVE_RELEASED.getCode())
                .orderBy(true, false, News::getCreateTime, News::getLastReplyTime));
        if(isNotEmpty(result.getRecords()))
            result.getRecords().forEach(o -> o.setLikeCount(thumbCache.countBy(o.getNewId(), null, null, 1)));
        return result;
    }

    /**
     * 查询关注页上的内容简要信息
     * @param userIds 用户id
     * @param current 第几页
     * @param size 几条
     * @return GetUserAccountDto.GetUserNewsDto
     */
    @Override
    public Page<GetUserNewsDto> getListByFollower(List<Long> userIds, Integer current, Integer size) {
        Page<GetUserNewsDto> result = selectJoinListPage(new Page<>(current, size), GetUserNewsDto.class, new MPJLambdaWrapper<News>()
                .selectAll(News.class)
                .eq(News::getStatus, NewCommentStatus.HAVE_RELEASED.getCode())
                .orderBy(true, false, News::getCreateTime)
                .in(News::getCreateId, userIds));

        if(isNotEmpty(result.getRecords()))
            result.getRecords().forEach(o -> o.setLikeCount(thumbCache.countBy(o.getNewId(), null, null, 1)));
        return result;
    }



    /**
     *根据主键查全部内容、点赞
     * @param newId 文章id
     * @return GetUserNewsDto
     */
    @Override
    public GetUserNewsDto getOneById(Long newId) {
        GetUserNewsDto result = selectJoinOne(GetUserNewsDto.class, new MPJLambdaWrapper<News>()
                .selectAll(News.class)
                .selectAssociation(NewContent.class, GetUserNewsDto::getContent, o -> o.result(NewContent::getContent))
                .leftJoin(NewContent.class, NewContent::getNewId, News::getNewId)
                .eq(News::getNewId, newId)
        );
        if(Objects.nonNull(result))
            result.setLikeCount(thumbCache.countBy(result.getNewId(), null, null, 1));
        return result;
    }



    @Resource
    public void setConverter(NewsConverter converter) {
        this.converter = converter;
    }

    @Resource
    public void setSensitiveFilter(SensitiveFilter sensitiveFilter) {
        this.sensitiveFilter = sensitiveFilter;
    }

    @Resource
    public void setThumbCache(ThumbCache thumbCache) {
        this.thumbCache = thumbCache;
    }
}
