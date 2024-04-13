package com.bbs.chat.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户账号信息
 * @TableName user
 */
@TableName(value ="user")
@Data
@Accessors(chain = true)
public class User implements Serializable {
    /**
     * 用户id
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 邮箱
     */
    @TableField(value = "email")
    private String email;

    /**
     * 用户名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 密码
     */
    @TableField(value = "password")
    private String password;

    /**
     * 手机号
     */
    @TableField(value = "phone")
    private Long phone;

    /**
     * 盐
     */
    @TableField(value = "salt")
    private Long salt;

    /**
     * 账号状态： 0：冻结1：正常 -1：封禁
     */
    @TableField(value = "state")
    private Integer state;

    /**
     * 过期时间
     */
    @TableField(value = "expiration_time")
    private Date expirationTime;

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
     * 头像
     */
    @TableField(value = "avatar")
    private String avatar;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}