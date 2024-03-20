package com.bbs.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.dto.GetUserAccountDto;
import com.bbs.dto.GetUserNewsDto;
import com.bbs.entity.Comment;
import com.bbs.entity.News;
import com.bbs.mapper.NewsMapper;
import com.bbs.service.NewsService;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
public class NewsServiceImpl extends MPJBaseServiceImpl<NewsMapper, News>
    implements NewsService{

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
        return wrapper
                .selectAll(News.class)
                .selectCollection(Comment.class, GetUserNewsDto::getCommentByNewIdDtoList)
                .leftJoin(Comment.class,Comment::getNewId,News::getNewId)
                .eq(News::getNewId,newId)
                .one();
    }


}




