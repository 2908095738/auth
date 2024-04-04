package com.bbs.content.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 评论表
 * @TableName comment
 */
@TableName(value ="comment")
@Data
@Accessors(chain = true)
public class Comment implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 父类id
     */
    @TableField(value = "parent_id")
    private Long parentId;

    /**
     * 父类类型
     */
    @TableField(value = "type")
    private Integer type;

    /**
     * 点赞数
     */
    @TableField(value = "like_count")
    private Integer likeCount;

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

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    /**
     * 暂无  状态 10.待审核 20.已发布 110.待审核用户删除 120.已发布用户删除 100010.待审核管理员删除 100020.已发布管理员删除
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 删除状态：0未删除  1已删除
     */
    @TableField(value = "delete_flag")
    private Integer deleteFlag;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}