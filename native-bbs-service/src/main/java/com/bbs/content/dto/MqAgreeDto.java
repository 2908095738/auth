package com.bbs.content.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 点赞通知实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MqAgreeDto implements Serializable {

    /**
     * 点赞用户id
     */
    private Long thumbUid;
    /**
     * 当前用户id
     */
    private Long uid;
    /**
     * 1.点赞评论 2.点赞文章
     */
    private  Integer type;
    /**
     * 时间
     */
    private Date time;

    private static final long serialVersionUID = 1L;

}
