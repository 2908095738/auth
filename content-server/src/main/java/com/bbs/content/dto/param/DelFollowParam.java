package com.bbs.content.dto.param;

import lombok.Data;

@Data
public class DelFollowParam {

    /**
     * 账号id
     */
    private Long userId;

    /**
     * 关注账号id
     */
    private Long followUserId;

    /**
     * 删除状态：0未删除  1已删除
     */
    private Integer deleteFlag = 1;

}
