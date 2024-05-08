package com.bbs.content.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bbs.content.dto.param.GetPageParam;
import com.bbs.content.entity.Tag;
import com.bbs.content.service.TagService;
import com.bbs.content.mapper.TagMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
        implements TagService {

    @Override
    public List<Long> addAndUpdateWeight(List<String> tagNames, List<Long> tagIds) {
        List<Tag> newTagList = new ArrayList<>();
        if(CollUtil.isNotEmpty(tagNames)){
            newTagList = tagNames.stream().map(
                    tagName -> new Tag()
                            .setName(tagName)
                            .setWeight(1.0)
            ).collect(Collectors.toList());
            saveBatch(newTagList);
        }
        if(CollUtil.isNotEmpty(tagIds)){
            List<Tag> tags = listByIds(tagIds);
            tags.forEach(tag -> tag.setWeight(tag.getWeight() + 1));
            updateBatchById(tags);
        }
        return newTagList.stream().map(Tag::getId).collect(Collectors.toList());
    }

    @Override
    public Page<Tag> getAllListPage(GetPageParam param) {
        return lambdaQuery().like(StringUtils.isNotBlank(param.getTagName()), Tag::getName, param.getTagName()).page(new Page<>(param.getCurrent(), param.getSize()));
    }


}




