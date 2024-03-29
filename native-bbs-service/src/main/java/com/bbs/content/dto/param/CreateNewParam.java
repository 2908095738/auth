package com.bbs.content.dto.param;

import lombok.Data;

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
