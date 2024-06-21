package com.bbs.auth.app.user.delete;

import com.bbs.Result;
import com.bbs.auth.cache.UserPermissionCache;
import com.bbs.auth.cache.code.PhoneCodeCache;
import com.bbs.auth.cache.user.PhoneCache;
import com.bbs.auth.cache.user.UserCache;
import com.bbs.auth.entity.User;
import com.bbs.auth.service.CompanyService;
import com.bbs.auth.service.TokenService;
import com.bbs.auth.service.UserService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping
public class UserDelete {

    @Resource
    private UserService userService;
    @Resource
    private TokenService tokenService;
    @Resource
    private UserCache userCache;
    @Resource
    private UserPermissionCache userPermissionCache;
    @Resource
    private PhoneCache phoneCache;
    @Resource
    private PhoneCodeCache phoneCodeCache;
    @Resource
    private CompanyService companyService;

    @DeleteMapping("/user")
    public Result<Boolean> delete() {
        // 查询登录用户
        User user = userService.loginEntityUser();

        Long loginUserId = user.getId();
        String phone = user.getPhone().toString();

        // 删除用户
        userService.removeById(loginUserId);
        // 删除公司
        companyService.deleteAllCompanyByUserId(loginUserId);

        // 删除缓存
        removeCache(loginUserId, phone);

        return Result.success();
    }

    private void removeCache(Long loginUserId, String phone) {
        tokenService.clearLoginFlag(loginUserId);
        userCache.remove(loginUserId);
        userPermissionCache.remove(loginUserId);
        phoneCache.remove(phone);
        phoneCodeCache.delCode(phone);
    }
}
