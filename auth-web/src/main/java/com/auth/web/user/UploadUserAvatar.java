package com.auth.web.user;

import com.auth.Result;
import com.auth.user.BaseController;
import com.auth.user.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 上传用户头像
 * @author ext.luchenlin5
 */
@Slf4j
@RestController
public class UploadUserAvatar extends BaseController {

    @Resource
    private User.Edit editUser;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Param {
        private String src;
    }

    /**
     * 头像上传
     */
    @PostMapping("/avatar")
    public Result<Boolean> upload(@RequestBody Param param) {
        editUser.updateAvatar(loginUserId(), param.src);
        return Result.success();
    }
}
