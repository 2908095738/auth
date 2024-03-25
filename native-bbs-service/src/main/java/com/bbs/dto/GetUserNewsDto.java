package com.bbs.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
@Data
public class GetUserNewsDto {

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
     * 全部内容
     */
    @TableField(value = "content")
    private String content;

    /**
     * 图片信息集合
     */
    @TableField(exist = false)
    private List<ImageInfo> imageInfoList = new ArrayList<>();

    /**
     * 媒体文件信息集合
     */
    @TableField(exist = false)
    private MediaInfo mediaInfoList;

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
     * 评论列表
     */
    @TableField(exist = false)
    private Page<CommentByNewIdDto> commentByNewIdDtoList;


    /**
     * 评论列表实体
     */
    @Data
    public class CommentByNewIdDto{

        /**
         *
         */
        @TableId(value = "id", type = IdType.AUTO)
        private Long id;

        /**
         * 点赞数(或直接取值，或统计数据库点赞数量)
         */
        @TableField(exist = false)
        private Long likeCount;

        /**
         * 评论内容
         */
        @TableField(value = "content")
        private String content;

        /**
         * 话题Id
         */
        @TableField(value = "new_id")
        private Long newId;

        /**
         * IP
         */
        @TableField(value = "ip")
        private String ip;

        /**
         * 评论人id
         */
        @TableField(value = "create_id")
        private Long createId;

        /**
         * 创建时间
         */
        @TableField(value = "create_time")
        private Date createTime;

    }

}
