package com.bbs.dto;

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
    @TableField(value = "id")
    private Long id;

    /**
     *
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
         * 内容摘要
         */
        @TableField(value = "summary")
        private String summary;

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
         * 评论数
         */
        @TableField(value = "comment_count")
        private Integer commentCount;

        /**
         * 点赞数
         */
        @TableField(value = "like_count")
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

    }

}
