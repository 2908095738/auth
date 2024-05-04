package com.bbs.content.dto.param;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;
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
    private List<String> summarys;

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
    private List<String> imageUrlList;

    /**
     * 视频
     */
    private List<String> viewUrlList;


    /**
     * 设定发布时间
     */
    private Date releaseTime;

    /**
     * 状态 0未保存 5草稿 10.待审核 15待定时发布 20.已发布 110.待审核用户删除 120.已发布用户删除 100010.待审核管理员删除 100020.已发布管理员删除
     */
    private Integer status;
}
