package com.bbs.content.service;

import com.bbs.content.dto.param.CreateFollowParam;
import com.bbs.content.dto.param.DelFollowParam;
import com.bbs.content.entity.Fan;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 *
 */
public interface FanService extends IService<Fan> {

    Boolean create(CreateFollowParam param);

    Boolean delFollow(DelFollowParam param);

    List<Fan> getFollow(Long userId);

    List<Fan> getFan(Long userId);
}
