package com.bbs.service;

import com.bbs.entity.NewContent;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 *
 */
public interface NewContentService extends IService<NewContent> {

    void createByNew(Long id, String content);
}
