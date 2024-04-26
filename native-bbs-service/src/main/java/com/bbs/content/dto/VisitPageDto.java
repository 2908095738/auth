package com.bbs.content.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.bbs.content.util.AuthUtil;
import lombok.Data;


@Data
public class VisitPageDto {

    /**
     *
     */
    @TableId(value = "id")
    private Long id;

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
     * 封面
     */
    @TableField(value = "summary")
    private String summary;

    @TableField(exist = false)
    private AuthUtil.UserAPI.VO user;




}
