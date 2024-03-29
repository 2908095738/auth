package com.bbs.content.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 文章内容
 * @TableName news
 */
@TableName(value ="news")
@Data
public class News implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "new_id", type = IdType.AUTO)
    private Long newId;

    /**
     * 标题
     */
    @TableField(value = "title")
    private String title;

    /**
     * 用户名
     */
    @TableField(value = "user_name")
    private String userName;

    /**
     * 内容摘要
     */
    @TableField(value = "summary")
    private String summary;

    /**
     * 图片
     */
    @TableField(value = "image_url")
    private String imageUrl;

    /**
     * 视频
     */
    @TableField(value = "view_url")
    private String viewUrl;

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
    private Integer commentCount = 0;

    /**
     * 最后回复时间
     */
    @TableField(value = "last_reply_time")
    private Date lastReplyTime;

    /**
     * 点赞数
     */
    @TableField(value = "like_count")
    private Integer likeCount = 0;

    /**
     * 状态 10.待审核 20.已发布 110.待审核删除 120.已发布删除
     */
    @TableField(value = "status")
    private Integer status = 10;

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
     * 修改id
     */
    @TableField(value = "update_id")
    private Long updateId;

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
     * 权重 =P表示热度因子(评论数+点赞数+浏览数)；T表示距离发帖的时间（单位为小时）；G表示"重力因子"（gravityth power），即将帖子排名往下拉的力量，默认值为1.8;  权重小于0时表示强制下沉，不再在热门榜上显示;  超出时间限制的热门话题权重在重新计算时设置为0        KaTeX公式表示：Score = \dfrac{P-1}{(T+2)^G}
     */
    @TableField(value = "weight")
    private Double weight;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}