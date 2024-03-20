package com.bbs.service.impl;

import com.bbs.dto.GetUserAccountDto;
import com.bbs.entity.Comment;
import com.bbs.entity.News;
import com.bbs.mapper.NewsMapper;
import com.bbs.service.NewsService;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 */
@Service
public class NewsServiceImpl extends MPJBaseServiceImpl<NewsMapper, News>
    implements NewsService{


    @Override
    public List<GetUserAccountDto.GetUserNewsDto> getListByUserId(Long userId) {
        MPJLambdaWrapper<GetUserAccountDto.GetUserNewsDto> wrapper = new MPJLambdaWrapper<>();
        return wrapper
                .selectAll(News.class)
                .selectCollection(Comment.class, GetUserAccountDto.GetUserNewsDto::getCommentByNewIdDtoList)
                .leftJoin(Comment.class,Comment::getNewId,News::getNewsId)
                .list();
    }


}




