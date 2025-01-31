package com.auth.web.user;

import com.auth.Result;
import com.auth.user.User;
import com.auth.user.dto.UserDTO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RequestMapping
@RestController("searchUserAPI")
public class SearchUser {

    @Resource
    private User.Search searchUser;

    @GetMapping("/user/page")
    public Result<Page<UserDTO>> page(
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "10") Integer size
    ) {
        return Result.success(searchUser.page(current, size));
    }
}
