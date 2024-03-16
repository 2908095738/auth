package com.bbs.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bbs.entity.News;
import com.bbs.service.NewsService;
import com.bbs.mapper.NewsMapper;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
public class NewsServiceImpl extends ServiceImpl<NewsMapper, News>
    implements NewsService{

}




