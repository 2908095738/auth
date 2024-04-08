package com.bbs.auth.app.user;

import com.bbs.Result;
import com.bbs.entity.UserVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping
public class User {


    /**
     * 当前用户个人信息
     * @return
     */
    @GetMapping
    public Result<UserVO> currentUserInfo(HttpServletRequest request) {
//        request.getHeader("token")
//        return service.search();
        return null;
    }
}
