package com.bbs.dto.param;

import lombok.Data;

import java.util.Date;

@Data
public class CreateNewParam {


    /**
     * 标题
     */
    private String title;

    /**
     * 内容摘要
     */
    private String summary;

    /**
     * 全部内容
     */
    private String content;


    /**
     * 标签id
     */
    private Integer tagId;

    /**
     * IP
     */
    private String ip;

    /**
     * 图片
     */
    private String imageUrl;

    /**
     * 视频
     */
    private String viewUrl;

    /**
     * 评论数
     */
    private Integer commentCount = 0;

    /**
     * 最后回复时间
     */
    private Date lastReplyTime = new Date();

    /**
     * 点赞数(或直接取值，或统计数据库点赞数量)
     */
    private Integer likeCount = 0;

    /**
     * 创建id
     */
    private Long createId;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 精华
     */
    private Integer essence;


}
