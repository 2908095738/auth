package com.bbs.content.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.content.entity.Tag;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 *
 */
public interface TagService extends IService<Tag> {

    Page<Tag> getAllListPage(Integer current, Integer size);
}
