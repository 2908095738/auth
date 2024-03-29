package com.bbs.content.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

@Data
public class GetUserAccountDto {

    /**
     *
     */
    @TableId(value = "user_id")
    private Long userId;


    /**
     * 第三方用户的唯一标识 例如微信的openid
     */
    @TableField(value = "open_id")
    private String openId;


    /**
     * 呢称
     */
    @TableField(exist = false)
    private String nickName;

    /**
     * 头像路径
     */
    @TableField(exist = false)
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

    /**
     * 店铺类型
     */
    @TableField(exist = false)
    private Integer storeType;

    /**
     * 营业执照
     */
    @TableField(exist = false)
    private String businessLicense;

    /**
     * 是否有店铺认证
     */
    @TableField(exist = false)
    private Integer storeCertification;

    /**
     * 个人标签
     */
    @TableField(exist = false)
    private String uTag;

    /**
     * 个人简介
     */
    @TableField(exist = false)
    private String remark;

    /**
     * 真实姓名
     */
    @TableField(exist = false)
    private String realName;

    /**
     * 性别
     */
    @TableField(exist = false)
    private Integer sex;

    /**
     * 年龄
     */
    @TableField(exist = false)
    private Integer age;

    /**
     * 手机号
     */
    @TableField(exist = false)
    private Long phone;

    /**
     * 证件号
     */
    @TableField(exist = false)
    private String idCard;


    /**
     * 文章列表
     */
    @TableField(exist = false)
    private Page<GetUserNewsDto> newsResult;


}
