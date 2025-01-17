package com.bbs.auth.app.role.system;

import cn.hutool.core.lang.tree.Tree;
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
public class SearchRoleMenuVO {

    private List<Tree<Long>> tree;

    private List<Long> roleMenuIds;
}
