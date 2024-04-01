package com.bbs.content.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bbs.content.entity.NewTag;
import com.bbs.content.mapper.NewTagMapper;
import com.bbs.content.service.NewTagService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@Service
public class NewTagServiceImpl extends ServiceImpl<NewTagMapper, NewTag>
    implements NewTagService{

    @Override
    public void createByNew(Long newId, List<Long> tagIds) {
        List<NewTag> newTags = new ArrayList<>();
        tagIds.forEach(id-> newTags.add(new NewTag(newId,id)));
        saveBatch(newTags);
    }
}




