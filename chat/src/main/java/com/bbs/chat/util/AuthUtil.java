package com.bbs.chat.util;

import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Component
public class AuthUtil {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Result {

        private String code;

        private String msg;

        private Object data;
    }

    @Slf4j
    @Component
    public static class UserAPI {

        private static final String TOKEN_HEADER_KEY = "Authorization";

        private static final String API_LOGIN_USER = "/system/user/profile";

        private static final Integer TIMEOUT = 10000;

        @Value("${auth.host}")
        private String host;

        @Resource
        private HttpServletRequest request;

        public User getLoginUser() {
            String token = request.getHeader(TOKEN_HEADER_KEY);
            if(isNotBlank(token)) {
                String serverHost = host + API_LOGIN_USER;
                try {
                    HttpResponse response = HttpRequest.get(serverHost).header(TOKEN_HEADER_KEY, token)
                            .timeout(TIMEOUT).execute();
                    if(response.isOk()) {
                        String body = response.body();
                        Result result = JSONUtil.toBean(body, Result.class);
                        if(String.valueOf(HttpStatus.HTTP_OK).equals(result.code)) {
                            Object data = result.getData();
                            if(nonNull(data)) {
                                User user = JSONUtil.parseObj(data).toBean(User.class);
                                log.debug("[AuthUtil.User::getLoginUser] 获取用户信息成功！！！ user={}", user);
                                return user;
                            }
                        }
                    }
                    return null;

                } catch (HttpException e) {
                    log.error("[AuthUtil.User::getLoginUser] 获取用户信息异常！！！");
                    throw new RuntimeException(e);
                }
            }
            return null;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class User {
            /** 用户ID */
            private Long userId;

            /** 部门ID */
            private Long deptId;

            /** 用户账号 */
            private String userName;

            /** 用户昵称 */
            private String nickName;

            /** 用户邮箱 */
            private String email;

            /** 手机号码 */
            private String phonenumber;

            /** 用户性别 */
            private String sex;

            /** 用户头像 */
            private String avatar;

            /** 密码 */
            private String password;

            /** 帐号状态（0正常 1停用） */
            private String status;

            /** 删除标志（0代表存在 2代表删除） */
            private String delFlag;

            /** 最后登录IP */
            private String loginIp;

            /** 最后登录时间 */
            private Date loginDate;

            /** 部门对象 */
            private SysDept dept;

            /** 角色对象 */
            private List<SysRole> roles;

            /** 角色组 */
            private Long[] roleIds;

            /** 岗位组 */
            private Long[] postIds;

            /** 角色ID */
            private Long roleId;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class SysDept {
            /** 部门ID */
            private Long deptId;

            /** 父部门ID */
            private Long parentId;

            /** 祖级列表 */
            private String ancestors;

            /** 部门名称 */
            private String deptName;

            /** 显示顺序 */
            private Integer orderNum;

            /** 负责人 */
            private String leader;

            /** 联系电话 */
            private String phone;

            /** 邮箱 */
            private String email;

            /** 部门状态:0正常,1停用 */
            private String status;

            /** 删除标志（0代表存在 2代表删除） */
            private String delFlag;

            /** 父部门名称 */
            private String parentName;

            /** 子部门 */
            private List<SysDept> children = new ArrayList<SysDept>();
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class SysRole {
            private Long roleId;

            /** 角色名称 */
            private String roleName;

            /** 角色权限 */
            private String roleKey;

            /** 角色排序 */
            private Integer roleSort;

            /** 数据范围（1：所有数据权限；2：自定义数据权限；3：本部门数据权限；4：本部门及以下数据权限；5：仅本人数据权限） */
            private String dataScope;

            /** 菜单树选择项是否关联显示（ 0：父子不互相关联显示 1：父子互相关联显示） */
            private boolean menuCheckStrictly;

            /** 部门树选择项是否关联显示（0：父子不互相关联显示 1：父子互相关联显示 ） */
            private boolean deptCheckStrictly;

            /** 角色状态（0正常 1停用） */
            private String status;

            /** 删除标志（0代表存在 2代表删除） */
            private String delFlag;

            /** 用户是否存在此角色标识 默认不存在 */
            private boolean flag = false;

            /** 菜单组 */
            private Long[] menuIds;

            /** 部门组（数据权限） */
            private Long[] deptIds;

            /** 角色菜单权限 */
            private Set<String> permissions;
        }
    }
}
