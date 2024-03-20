package com.bbs.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 点赞
 * @TableName thumb
 */
@TableName(value ="thumb")
@Data
public class Thumb implements Serializable {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 话题或评论id
     */
    @TableField(value = "tc_id")
    private Long tcId;

    /**
     * 发布话题或评论的用户id
     */
    @TableField(value = "post_user_id")
    private Long postUserId;

    /**
     * 话题或评论点赞的用户id
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 修改时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    /**
     * 点赞内容摘要
     */
    @TableField(value = "tc_summary")
    private String tcSummary;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}