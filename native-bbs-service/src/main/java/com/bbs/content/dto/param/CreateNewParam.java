package com.bbs.content.dto.param;

import lombok.Data;

import java.util.List;

@Data
public class CreateNewParam {

    /**
     * 内容id
     */
    private Long newId;


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
     * 标签ids
     */
    private List<Long> tagIds;

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
     * 排序
     */
    private Integer sort;

    /**
     * 精华
     */
    private Integer essence;


}
