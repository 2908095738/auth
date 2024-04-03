package com.bbs.content.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.content.dto.param.GetPageParam;
import com.bbs.content.entity.Tag;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 *
 */
public interface TagService extends IService<Tag> {

    List<Long> addAndUpdateWeight(List<String> tagNames, List<Long> tagIds);

    Page<Tag> getAllListPage(GetPageParam param);

}
