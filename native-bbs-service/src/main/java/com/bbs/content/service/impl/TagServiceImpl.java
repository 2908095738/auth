package com.bbs.content.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bbs.content.entity.Tag;
import com.bbs.content.service.TagService;
import com.bbs.content.mapper.TagMapper;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
    implements TagService{

    @Override
    public Page<Tag> getAllListPage(Integer current, Integer size) {
        return lambdaQuery().page(new Page<>(current,size));
    }
}




