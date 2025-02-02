package com.auth.log.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@TableName(value ="log_login")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginLog {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 登录设备类型编码
     */
    @TableField(value = "device_type")
    private String deviceType;

    /**
     * 登录 IP
     */
    @TableField(value = "ip")
    private String ip;

    /**
     * 登录时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * Request 唯一ID
     */
    @TableField(value = "request_id")
    private String requestId;

    /**
     * 登录耗时
     */
    @TableField(value = "trip_time")
    private Long tripTime;

    public LoginLog(Long userId, Date createTime, String ip, String requestId, Long tripTime) {
        this.userId = userId;
        this.createTime = createTime;
        this.ip = ip;
        this.requestId = requestId;
        this.tripTime = tripTime;
    }
}