package com.auth.user.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 用户信息
 * @author ext.luchenlin5
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名称
     */
    private String name;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 性别
     */
    private Integer sex;

    /**
     * 个性签名
     */
    private String sign;

    /**
     * 密码
     */
    private String password;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 手机号
     */
    private Long phone;

    /**
     * 工号
     */
    private String jobCard;


    /**
     * 账号状态： 1：冻结0：正常 -1：封禁 2：离职
     */
    private Integer state;

    /**
     * 微信 OpenID
     */
    private String openId;

    /**
     * 账号状态
     */
    private String stateStr;

    /**
     * 过期时间
     */
    private Date expirationTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 创建人
     */
    private Long createBy;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 创建人
     */
    private Long updateBy;

    /**
     * 图片 URL 地址
     */
    private String avatar;

    /**
     * 平台角色
     */
    private Integer paasRole;

    /**
     * 个人首页背景图片
     */
    private String backgroundImage;

    /**
     * 是否是超级管理员
     */
    private Integer isAdmin;

    /**
     * 角色ID
     */
    private Integer role;

    /**
     * 盐
     */
    private Integer salt;

    public UserDTO(String inputPhone) {
        this.name = "用户 " + phone;
        this.phone = Long.valueOf(inputPhone);
    }

    public UserDTO(String inputPhone, String openId) {
        this.name = "用户 " + phone;
        this.phone = Long.valueOf(inputPhone);
        this.openId = openId;
    }
}
