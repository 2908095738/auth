package com.bbs.content.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bbs.content.dto.GetUserNewsDto;
import com.bbs.content.dto.param.CreateNewParam;
import com.bbs.content.dto.param.QueryNewsParam;
import com.bbs.content.entity.News;

/**
 *
 */
public interface NewsService extends IService<News> {


    News createNews(CreateNewParam param);


    Page<GetUserNewsDto> getListByQuery(QueryNewsParam param);

    Page<GetUserNewsDto> getListByUserId(Long userId, Integer current, Integer size, boolean flag);

    GetUserNewsDto getOneById(Long newId);

    Page<GetUserNewsDto> getListByRecommend(Integer current, Integer size);

    Page<GetUserNewsDto> getListByFollower(Long userIds, Integer current, Integer size);

    Long createNewsId(Long createId, String userName);

    void delete(Long newId, Long userId);
}
