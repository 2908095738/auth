package com.auth.rbac.user.role.enetity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * RBAC-用户角色关联
 * @author ext.luchenlin5
 */
@TableName(value ="rbac_user_role")
@Data
@Accessors(chain = true)
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
    private String userId;

    /**
     * 角色ID
     */
    @TableField(value = "role_id")
    private String roleId;
}