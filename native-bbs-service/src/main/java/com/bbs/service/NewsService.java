package com.bbs.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bbs.dto.GetUserAccountDto;
import com.bbs.dto.GetUserNewsDto;
import com.bbs.dto.param.CreateNewParam;
import com.bbs.entity.News;

import java.util.List;

/**
 *
 */
public interface NewsService extends IService<News> {


    Long createNews(CreateNewParam param);

    Page<GetUserAccountDto.GetUserNewsDto> getListByUserId(Long userId, Integer current, Integer size, boolean flag);

    GetUserNewsDto getOneById(Long newId);

    Page<GetUserAccountDto.GetUserNewsDto> getListByRecommend(Integer current, Integer size);

    Page<GetUserAccountDto.GetUserNewsDto> getListByFollower(List<Long> userIds, Integer current, Integer size);
}
