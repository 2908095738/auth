package com.bbs.log.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bbs.log.entity.Content;
import com.bbs.log.service.ContentService;
import com.bbs.log.mapper.ContentMapper;
import org.springframework.stereotype.Service;

/**
* @author 路晨霖
* @description 针对表【content(内容表)】的数据库操作Service实现
* @createDate 2024-05-05 12:51:12
*/
@Service
public class ContentServiceImpl extends ServiceImpl<ContentMapper, Content>
    implements ContentService{

}




