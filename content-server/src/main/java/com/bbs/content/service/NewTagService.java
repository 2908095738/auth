package com.bbs.content.service;

import com.bbs.content.entity.NewTag;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 *
 */
public interface NewTagService extends IService<NewTag> {

    void createByNew(Long newId, List<Long> tagIds);
}
