package com.bbs.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bbs.auth.app.follow.DelFollow;
import com.bbs.auth.app.follow.Follow;
import com.bbs.auth.entity.Fan;

import java.util.List;

/**
 * 关注粉丝群
 */
public interface FanService extends IService<Fan> {

    Boolean create(Follow.Param param);

    Boolean delFollow(DelFollow.Param param);

    List<Fan> getFollow(Long userId);

    /**
     * 查询粉丝、谁关注了我（userId）
     * @param userId 用户ID
     * @return 关注了 userID 的用户
     */
    List<Fan> getFan(Long userId);
}
