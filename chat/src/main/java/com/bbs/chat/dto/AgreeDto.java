package com.bbs.chat.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.Date;

/**
 * 点赞、收藏实体
 */
@Data
public class AgreeDto {

    /**
     * 点赞、收藏id
     * (用户点击查看被点赞、收藏内容)
     */
    private Long agreeId;

    /**
     * 点赞、收藏用户id
     */
    private Long agreeUid;

    /**
     * 点赞、收藏用户呢称
     */
    private String name;

    //TODO impl ing
    /**
     * 点赞、收藏用户头像路径
     */
    @TableField(value = "avatar_path")
    private String avatarPath;

    /**
     * 点赞/收藏标识符
     * 1：点赞文章
     * 2：点赞评论
     * 3：收藏文章
     * 优先级不高，可能只是想看看谁进行了点赞、收藏，后续查询具体的被点赞、收藏内容
     */
    @TableField(value = "type")
    private Integer type;

    /**
     * 点赞、收藏时间
     */
    @TableField(value = "time")
    private Date time;
}