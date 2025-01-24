package com.auth.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 登录拦截忽略配置
 * @author ext.luchenlin5
 */
@TableName(value ="login_intercept_ignore")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginInterceptIgnoreConfig implements Serializable {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 接口路径
     */
    @TableField(value = "api_path")
    private String apiPath;

    /**
     * 类路径
     */
    @TableField(value = "class_path")
    private String classPath;

    /**
     * 描述
     */
    @TableField(value = "description")
    private String description;

    /**
     * 状态
     */
    @TableField(value = "status")
    private String status;
}
