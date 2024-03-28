package com.bbs.content.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.util.Date;

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

    /**
     * 文章列表实体
     */
    @Data
    public class GetUserNewsDto{

        /**
         * 主键
         */
        @TableId(value = "news_id", type = IdType.AUTO)
        private Long newsId;

        /**
         * 标题
         */
        @TableField(value = "title")
        private String title;

        /**
         * 标签id
         */
        @TableField(value = "tag_id")
        private Integer tagId;

        /**
         * IP
         */
        @TableField(value = "ip")
        private String ip;

        /**
         * 点赞数
         */
        @TableField(exist = false)
        private Integer likeCount;

        /**
         * 创建id
         */
        @TableField(value = "create_id")
        private Long createId;

        /**
         * 发表时间
         */
        @TableField(value = "create_time")
        private Date createTime;

        /**
         * 修改时间
         */
        @TableField(value = "update_time")
        private Date updateTime;

        /**
         * 排序
         */
        @TableField(value = "sort")
        private Integer sort;

        /**
         * 精华
         */
        @TableField(value = "essence")
        private Integer essence;

        /**
         * 状态 10.待审核 20.已发布 110.待审核用户删除 120.已发布用户删除 100010.待审核管理员删除 100020.已发布管理员删除
         */
        @TableField(value = "status")
        private Integer status;

    }

}
