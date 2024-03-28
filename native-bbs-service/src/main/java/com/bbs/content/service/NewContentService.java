package com.bbs.content.service;

import com.bbs.content.entity.NewContent;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 *
 */
public interface NewContentService extends IService<NewContent> {

    void createByNew(Long id, String content);
}
