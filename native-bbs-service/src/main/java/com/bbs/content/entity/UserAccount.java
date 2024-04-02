package com.bbs.content.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 用户账号信息
 * @TableName user_account
 */
@TableName(value ="user_account")
@Data
@Accessors(chain = true)
public class UserAccount implements Serializable {

    /**
     * 用户id
     */
    @TableId(value = "user_id")
    private Long userId;

    /**
     * 等级
     */
    @TableField(value = "rank")
    private Integer rank;

    /**
     * 总积分
     */
    @TableField(value = "score")
    private Integer score;

    /**
     * 被点赞数
     */
    @TableField(value = "like_count")
    private Integer likeCount;

    /**
     * 关注数
     */
    @TableField(value = "follower_count")
    private Integer followerCount;

    /**
     * 粉丝数
     */
    @TableField(value = "fan_count")
    private Integer fanCount;

    /**
     * 收藏数
     */
    @TableField(value = "favorite_count")
    private Integer favoriteCount;

    /**
     * 浏览数
     */
    @TableField(value = "page_view_count")
    private Integer pageViewCount;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}