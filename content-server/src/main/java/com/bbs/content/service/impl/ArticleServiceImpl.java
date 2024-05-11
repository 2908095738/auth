package com.bbs.content.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bbs.content.entity.Article;
import com.bbs.content.service.ArticleService;
import com.bbs.content.mapper.ArticleMapper;
import org.springframework.stereotype.Service;

/**
* @author 路晨霖
* @description 针对表【article(内容表)】的数据库操作Service实现
* @createDate 2024-05-05 13:58:55
*/
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article>
    implements ArticleService {

}




