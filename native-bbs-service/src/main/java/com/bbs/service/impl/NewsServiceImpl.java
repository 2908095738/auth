package com.bbs.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.converter.NewsConverter;
import com.bbs.dto.GetUserAccountDto;
import com.bbs.dto.GetUserNewsDto;
import com.bbs.dto.param.CreateNewParam;
import com.bbs.entity.Comment;
import com.bbs.entity.News;
import com.bbs.entity.Thumb;
import com.bbs.mapper.NewsMapper;
import com.bbs.service.NewsService;
import com.bbs.util.SensitiveFilter;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;
import java.util.Objects;

/**
 *
 */
@Service
public class NewsServiceImpl extends MPJBaseServiceImpl<NewsMapper, News>
    implements NewsService{

    private NewsConverter converter;

    public SensitiveFilter sensitiveFilter;

    @Override
    public void createNews(CreateNewParam param) {
       News news = converter.toEntity(param);
       // 转义 HTML 标记，防止在 HTML 标签中注入攻击语句
        news.setTitle(HtmlUtils.htmlEscape(news.getTitle()));
        news.setContent(HtmlUtils.htmlEscape(news.getContent()));
       // 过滤敏感词
        news.setTitle(sensitiveFilter.filter(news.getTitle()));
        news.setContent(sensitiveFilter.filter(news.getContent()));
        save(news);
    }

    /**
     *查询用户主页上发布内容集合
     * @param userId
     * @return
     */
    @Override
    public Page<GetUserAccountDto.GetUserNewsDto> getListByUserId(Long userId, Integer current, Integer size) {
        return selectJoinListPage(new Page<>(current, size),GetUserAccountDto.GetUserNewsDto.class,new MPJLambdaWrapper<News>()
                .selectAll(News.class)
                .eq(News::getCreateId,userId));
    }

    /**
     *根据主键查全部内容、评论、点赞
     * @param newId
     * @return
     */
    @Override
    public GetUserNewsDto getOneById(Long newId) {
        MPJLambdaWrapper<GetUserNewsDto> wrapper = new MPJLambdaWrapper<>();
        GetUserNewsDto newsDto = wrapper
                .selectAll(News.class)
                .selectCount(Thumb::getId, News::getLikeCount)
                .selectCollection(Comment.class, GetUserNewsDto::getCommentByNewIdDtoList)
                .leftJoin(Comment.class, Comment::getNewId, News::getNewId)
                .leftJoin(Thumb.class, Thumb::getTcId, News::getNewId)
                .eq(News::getNewId, newId)
                .one();
        if(Objects.nonNull(newsDto)&& CollectionUtil.isNotEmpty(newsDto.getCommentByNewIdDtoList())){
            newsDto.getCommentByNewIdDtoList().forEach(o->{
                LambdaQueryChainWrapper<Thumb> thumbQueryWrapper = new LambdaQueryChainWrapper<>(Thumb.class);
                o.setLikeCount(thumbQueryWrapper.select(Thumb::getTcId).eq(Thumb::getTcId,o.getId()).count());
            });
        }
        return newsDto;
    }

    @Resource
    public void setConverter(NewsConverter converter) {
        this.converter = converter;
    }

    @Resource
    public void setSensitiveFilter(SensitiveFilter sensitiveFilter) {
        this.sensitiveFilter = sensitiveFilter;
    }


}
