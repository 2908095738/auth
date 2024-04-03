package com.bbs.content.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 评论通知实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MqCommentDto  implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 父类id
     */
    private Long parentId;


    /**
     * 评论内容
     */
    private String content;

    /**
     * 内容Id
     */
    private Long newId;

    /**
     * 评论人id
     */
    private Long createId;

    /**
     * 时间
     */
    private Date time = new Date();


}
