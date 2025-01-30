package com.auth.rbac.user.role.enetity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@TableName("role_menu")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleMenuEntity implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField
    private Long roleId;

    @TableField
    private Long menuId;

    public RoleMenuEntity(Long roleId, Long menuId) {
        this.roleId = roleId;
        this.menuId = menuId;
    }
}
