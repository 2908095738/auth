package com.bbs.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户
 * @TableName user
 */
@TableName(value ="user")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User implements Serializable {
    /**
     * 用户ID
     */
    @TableId(value = "id", type = IdType.AUTO)
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
    private Integer salt;

    /**
     * 账号状态
     */
    @TableField(value = "state")
    private Integer state;

    /**
     * 账号状态
     */
    @TableField(exist = false)
    private String stateStr;

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
     * 图片 URL 地址
     */
    @TableField(value = "avatar")
    private String avatar;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableField(exist = false)
    private List<UserGroup> userGroupList;

    public User(Long id, String email, String name, String password, Long phone, Integer salt, Integer state, String stateStr, Date expirationTime, Date createTime, Date updateTime, List<UserGroup> userGroupList) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.password = password;
        this.phone = phone;
        this.salt = salt;
        this.state = state;
        this.stateStr = stateStr;
        this.expirationTime = expirationTime;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.userGroupList = userGroupList;
    }
}