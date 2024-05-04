package com.bbs.content.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bbs.content.dto.VisitPageDto;
import com.bbs.content.entity.VisitPage;

/**
 *
 */
public interface VisitPageService extends IService<VisitPage> {

    Page<VisitPageDto> getListByUserId(Integer current, Integer size, Long currentUserId, String title);

    void createVisitPage(Long currentUserId, Long newId);

}
