package com.bbs.dto.param;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

@Data
public class UpdateAccountParam {


    /**
     *
     */
    @TableField(value = "id")
    private Long id;

    /**
     * 呢称
     */
    @TableField(value = "nick_name")
    private String nickName;

    /**
     * 头像路径
     */
    @TableField(value = "avatar_path")
    private String avatarPath;


    /**
     * 性别
     */
    @TableField(value = "sex")
    private Integer sex;

    /**
     * 年龄
     */
    @TableField(value = "age")
    private Integer age;

    /**
     * 手机号
     */
    @TableField(value = "phone")
    private Long phone;

    /**
     * 个人简介
     */
    @TableField(value = "remark")
    private String remark;

    /**
     * 个人标签
     */
    @TableField(value = "u_tag")
    private String uTag;
}
