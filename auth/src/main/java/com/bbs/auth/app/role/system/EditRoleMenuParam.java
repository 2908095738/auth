package com.bbs.auth.app.role.system;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author ext.luchenlin5
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EditRoleMenuParam {

    private Long roleId;

    private List<Long> menuIds;
}
