package com.bbs.content.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.bbs.content.util.AuthUtil;
import lombok.Data;

import java.util.Date;

@Data
public class GetContentDto {

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
     * 封面
     */
    @TableField(value = "summary")
    private String summary;


    /**
     * IP
     */
    @TableField(value = "ip")
    private String ip;


    /**
     * 地址
     */
    @TableField(value = "addr")
    private String addr;

    /**
     * 评论数
     */
    @TableField(value = "comment_count")
    private Integer commentCount;

    /**
     * 最后回复时间
     */
    @TableField(value = "last_reply_time")
    private Date lastReplyTime;

    /**
     * 点赞数(或直接取值，或统计数据库点赞数量)
     */
    @TableField(exist = false)
    private Integer likeCount;

    /**
     * 浏览数
     */
    @TableField(exist = false)
    private Integer visitNum;

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

    @TableField(exist = false)
    private AuthUtil.UserAPI.VO user;



}
