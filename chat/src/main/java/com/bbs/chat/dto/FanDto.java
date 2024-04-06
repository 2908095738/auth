package com.bbs.chat.dto;

import lombok.Data;

import java.util.Date;

/**
 * 新增关注
 */
@Data
public class FanDto {
    /**
     * 新增关注用户id
     */
    private Integer fanUid;

    /**
     * 新增关注呢称
     */
    private String nickName;

    /**
     * 新增关注像路径
     */
    private String avatarPath;

    /**
     * 类型：0粉丝 3互关
     */
    private Integer type;

    /**
     * 新增关注时间
     */
    private Date time;
}