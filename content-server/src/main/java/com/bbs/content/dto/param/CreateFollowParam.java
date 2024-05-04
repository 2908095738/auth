package com.bbs.content.dto.param;

import lombok.Data;

@Data
public class CreateFollowParam {

    /**
     * 账号id
     */
    private Long userId;

    /**
     * 关注账号id
     */
    private Long followUserId;


}
