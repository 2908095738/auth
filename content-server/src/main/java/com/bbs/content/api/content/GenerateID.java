package com.bbs.content.api.content;

import com.bbs.Result;
import com.bbs.api.Auth;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 内容唯一 ID
 */
@RestController
@RequestMapping
public class GenerateID {

    @Resource
    private Auth.UserAPI userAPI;

    @GetMapping("/id")
    public Result<String> generate() {
        Auth.UserAPI.User loginUser = userAPI.getLoginUser();
        String id = String.valueOf(loginUser.getId() + System.currentTimeMillis());
        return Result.success(id);
    }
}
