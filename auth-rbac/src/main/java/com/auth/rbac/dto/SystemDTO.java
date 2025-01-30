package com.auth.rbac.dto;

import com.auth.user.dto.UserDTO;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemDTO {

    /**
     * 主键
     */
    private Long id;

    /**
     * 编码
     */
    private String code;

    /**
     * 名称
     */
    private String name;

    /**
     * 管理员
     */
    private Long adminId;

    /**
     * 管理员
     */
    private UserDTO admin;

    /**
     * 描述
     */
    private String description;

    /**
     * 父级系统
     */
    private Long pid;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 信息创建人 ID
     */
    private Long createBy;

    /**
     * 信息创建人
     */
    private UserDTO createUser;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 信息修改人 ID
     */
    private Long updateBy;

    /**
     * 信息修改人
     */
    private UserDTO updateUser;

    /**
     * 状态
     */
    private Integer state;

    /**
     * 是否隐藏
     */
    @TableField(value = "is_hide")
    private Integer isHide;

    /**
     * icon
     */
    @TableField(value = "icon_name")
    private String iconName;
}
