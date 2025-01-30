package com.auth.rbac.user.role.enetity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * RBAC-用户角色关联
 * @author ext.luchenlin5
 */
@TableName(value ="rbac_user_role")
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleEntity implements Serializable {
    /**
     * 编号
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 用户ID
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 系统编码
     */
    @TableField(value = "system_code")
    private String systemCode;

    /**
     * 角色ID
     */
    @TableField(value = "role_id")
    private Long roleId;

    public UserRoleEntity(Long userId, String systemCode, Long roleId) {
        this.userId = userId;
        this.systemCode = systemCode;
        this.roleId = roleId;
    }
}