package com.clinic.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 
 * @TableName question
 */
@Data
public class Question implements Serializable {
    /**
     * 
     */
    private Long id;

    /**
     * 
     */
    private Date createdAt;

    /**
     * 
     */
    private Date updatedAt;

    /**
     * 
     */
    private Long userId;

    /**
     * 
     */
    private String inviteUserId;

    /**
     * 
     */
    private Long lastEditUserId;

    /**
     * 
     */
    private String title;

    /**
     * 
     */
    private String originalText;

    /**
     * 
     */
    private String parsedText;

    /**
     * 
     */
    private Integer pin;

    /**
     * 
     */
    private Integer show;

    /**
     * 
     */
    private Integer status;

    /**
     * 
     */
    private Integer viewCount;

    /**
     * 
     */
    private Integer uniqueViewCount;

    /**
     * 
     */
    @TableField(value = "vote_count")
    private Integer voteCount;

    /**
     * 
     */
    @TableField(value = "answer_count")
    private Integer answerCount;

    /**
     * 
     */
    @TableField(value = "hot_score")
    private Integer hotScore;

    /**
     * 
     */
    @TableField(value = "collection_count")
    private Integer collectionCount;

    /**
     * 
     */
    @TableField(value = "follow_count")
    private Integer followCount;

    /**
     * 
     */
    @TableField(value = "accepted_answer_id")
    private Long acceptedAnswerId;

    /**
     * 
     */
    @TableField(value = "last_answer_id")
    private Long lastAnswerId;

    /**
     * 
     */
    @TableField(value = "post_update_time")
    private Date postUpdateTime;

    /**
     * 
     */
    @TableField(value = "revision_id")
    private Long revisionId;

    private List<Answer> answers;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}