package com.bbs.chat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bbs.Result;
import com.bbs.chat.dto.MqFollowDto;
import com.bbs.chat.entity.Fan;

public interface FollowService extends IService<Fan> {
    Result createFollow(Fan fan);

    Result cancelFollow(Fan fan);
}