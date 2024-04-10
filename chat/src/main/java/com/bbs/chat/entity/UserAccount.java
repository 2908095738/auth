package com.bbs.chat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户实名信息
 * @TableName user_account
 */
@TableName(value ="user_account")
@Data
public class UserAccount implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 第三方用户的唯一标识 例如微信的openid
     */
    @TableField(value = "open_id")
    private String openId;

    /**
     * 呢称
     */
    @TableField(value = "nick_name")
    private String nickName;

    /**
     * 头像路径
     */
    @TableField(value = "avatar_path")
    private String avatarPath;

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
    private Long likeCount;

    /**
     * 关注数
     */
    @TableField(value = "follower_count")
    private Long followerCount;

    /**
     * 粉丝数
     */
    @TableField(value = "fan_count")
    private Long fanCount;

    /**
     * 收藏数
     */
    @TableField(value = "favorite_count")
    private Long favoriteCount;

    /**
     * 店铺类型
     */
    @TableField(value = "store_type")
    private Integer storeType;

    /**
     * 营业执照
     */
    @TableField(value = "business_license")
    private String businessLicense;

    /**
     * 是否有店铺认证
     */
    @TableField(value = "store_certification")
    private Integer storeCertification;

    /**
     * 个人标签
     */
    @TableField(value = "u_tag")
    private String uTag;

    /**
     * 个人简介
     */
    @TableField(value = "remark")
    private String remark;

    /**
     * 记住密码
     */
    @TableField(value = "remember_me")
    private Integer rememberMe;

    /**
     * 安全摘要
     */
    @TableField(value = "security_digest")
    private Long securityDigest;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}