package com.bbs.content.dto.param;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class CreateNewParam {

    /**
     * 内容id
     */
    @NotNull(message = "内容id不能为空！")
    private Long newId;


    /**
     * 标题
     */
    @NotNull(message = "标题不能为空！")
    private String title;

    /**
     * 封面
     */
    @NotNull(message = "封面不能为空！")
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
     * 自定义标签名
     */
    private List<String> tagNames;

    /**
     * IP
     */
    private String ip;

    /**
     * 地址
     */
    private String addr;

    /**
     * 图片
     */
    private List<String> imageUrl;

    /**
     * 视频
     */
    private List<String> viewUrl;



}
