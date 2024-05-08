package com.bbs.content.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;
@Data
public class GetFavoritesDto {


    /**
     *
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 内容id
     */
    @TableField(value = "new_id")
    private Long newId;

    /**
     * 标题
     */
    @TableField(value = "title")
    private String title;

    /**
     * 用户名
     */
    @TableField(value = "user_name")
    private String userName;

    /**
     * 封面
     */
    @TableField(value = "summary")
    private String summary;

    /**
     * 点赞数
     */
    @TableField(value = "like_count")
    private Integer likeCount;


    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;






}
