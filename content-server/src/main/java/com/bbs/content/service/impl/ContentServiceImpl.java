package com.bbs.content.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bbs.content.entity.Content;
import com.bbs.content.service.ContentService;
import com.bbs.content.mapper.ContentMapper;
import org.springframework.stereotype.Service;

/**
* @author 路晨霖
* @description 针对表【content(内容)】的数据库操作Service实现
* @createDate 2024-05-05 15:14:55
*/
@Service
public class ContentServiceImpl extends ServiceImpl<ContentMapper, Content>
    implements ContentService{

}




