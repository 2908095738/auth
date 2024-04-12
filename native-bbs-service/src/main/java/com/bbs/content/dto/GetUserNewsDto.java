package com.bbs.content.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.content.util.AuthUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetUserNewsDto {

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
     * 内容摘要
     */
    @TableField(value = "summary")
    private String summary;

    /**
     * 全部内容
     */
    @TableField(exist = false)
    private String content;

    /**
     * 标签ids
     */
    @TableField(exist = false)
    private List<Long> tagIds;

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

    @TableField(exist = false)
    private AuthUtil.UserAPI.VO user;

    /**
     * 评论列表实体
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CommentByNewIdDto{

        /**
         * 评论ID
         */
        @TableId(value = "id", type = IdType.AUTO)
        private Long id;

        /**
         * 父类id
         */
        @TableField(value = "parent_id")
        private Long parentId;

        /**
         * 点赞数(或直接取值，或统计数据库点赞数量)
         */
        @TableField(exist = false)
        private Integer likeCount;

        /**
         * 自己（当前登录用户）是否点赞了
         */
        private Boolean hasLike;

        /**
         * 能不能删除该评论（创建人是当前用户 or 当前用户为管理员）
         */
        private Boolean owner;

        /**
         * 内容
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
         * 评论者id
         */
        @TableField(value = "create_id")
        private Long createId;

        /**
         * 评论者昵称
         */
        private String nickName;

        /**
         * 评论者头像地址
         */
        private String avatarUrl;

        /**
         * 创建时间
         */
        @TableField(value = "create_time")
        private Date createTime;
    }

}
