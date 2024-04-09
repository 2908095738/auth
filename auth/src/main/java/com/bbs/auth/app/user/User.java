package com.bbs.auth.app.user;

import com.bbs.Result;
import com.bbs.auth.converter.UserConverter;
import com.bbs.auth.service.TokenService;
import com.bbs.auth.service.UserService;
import com.bbs.entity.UserVO;
import com.bbs.exception.ReLoginException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping
public class User {


    @Resource
    private TokenService service;

    @Resource
    private UserService userService;

    @Resource
    private UserConverter converter;

    /**
     * 当前用户个人信息
     */
    @GetMapping
    public Result<UserVO> currentUserInfo(HttpServletRequest request) throws ReLoginException {
        String token = service.getToken(request);
        UserVO vo = service.verify(token);
        return Result.success(converter.toVO(userService.search(vo.getId())));
    }
}
