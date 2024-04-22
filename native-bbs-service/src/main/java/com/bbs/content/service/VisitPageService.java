package com.bbs.content.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.content.dto.GetContentDto;
import com.bbs.content.entity.VisitPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 *
 */
public interface VisitPageService extends IService<VisitPage> {

    Page<GetContentDto> getListByUserId(Integer current, Integer size, Long currentUserId, String title);

    void createVisitPage(Long currentUserId, Long newId);

}
