package com.bbs.content.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bbs.content.dto.GetContentDto;
import com.bbs.content.dto.GetUserNewsDto;
import com.bbs.content.dto.param.CreateNewParam;
import com.bbs.content.entity.News;

import java.util.List;

/**
 *
 */
public interface NewsService extends IService<News> {

    Long createNewsId(Long createId, String userName,Integer type);
    News createNews(CreateNewParam param);

    Page<GetContentDto> getListByRecommend(Integer current, Integer size);
    Page<GetContentDto> getListByFollower(Integer current, Integer size, List<Long> userIds, String title);
    Page<GetContentDto> getListByNative(Integer current, Integer size, String city, String title);
    Page<GetContentDto> getListByUserId(Integer current, Integer size, Long userId, Integer type, boolean flag, String title);


    GetUserNewsDto getOneById(Long newId);

    void delete(Long newId, Long userId);

    void updateStatus(Integer status, Long newId);


}
