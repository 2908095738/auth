package com.bbs.log.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@TableName(value ="log_login")
@Data
public class LogLogin implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 登录结果（0正常/1失败）
     */
    @TableField(value = "result")
    private Integer result;

    /**
     * 手机号
     */
    @TableField(value = "phone")
    private String phone;

    /**
     * 输入验证码
     */
    @TableField(value = "code")
    private String code;

    /**
     * 登录类型
     */
    @TableField(value = "login_type")
    private Integer loginType;

    /**
     * 服务端保存的手机验证码
     */
    @TableField(value = "server_save_phone_code")
    private String serverSavePhoneCode;

    /**
     * 登录成功生成的 Token
     */
    @TableField(value = "new_token")
    private String newToken;

    /**
     * 失败原因
     */
    @TableField(value = "errorMsg")
    private String errormsg;

    /**
     * 登录时间
     */
    @TableField(value = "login_time")
    private Date loginTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}