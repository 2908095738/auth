package com.bbs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bbs.dto.GetUserAccountDto;
import com.bbs.entity.News;

import java.util.List;

/**
 *
 */
public interface NewsService extends IService<News> {

    List<GetUserAccountDto.GetUserNewsDto> getListByUserId(Long userId);
}
