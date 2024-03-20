package com.bbs.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bbs.dto.GetUserAccountDto;
import com.bbs.dto.GetUserNewsDto;
import com.bbs.entity.News;

/**
 *
 */
public interface NewsService extends IService<News> {

    /**
     *查询用户主页上发布内容集合
     * @param userId
     * @return
     */
    Page<GetUserAccountDto.GetUserNewsDto> getListByUserId(Long userId, Integer current, Integer size);


    /**
     *根据主键查全部内容、评论、点赞
     * @param newId
     * @return
     */
    GetUserNewsDto getOneById(Long newId);
}
