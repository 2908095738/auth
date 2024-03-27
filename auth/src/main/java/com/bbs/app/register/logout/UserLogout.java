package com.bbs.app.register.logout;

import com.clinic.Result;
import com.bbs.service.TokenService;
import com.bbs.entity.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping
public class UserLogout {

    private final TokenService tokenService;

    @DeleteMapping("/user")
    public Result<Boolean> logout(HttpServletRequest request) {
        String token = tokenService.getToken(request);
        if(StringUtils.isNotBlank(token) && tokenService.verifyToken(token)) {
            UserVO userVO = tokenService.parseToken(token);
            tokenService.clearLoginFlag(userVO.getId());
            return Result.success();
        }
        return Result.failed(500, "账号登出失败，请联系客服！");
    }

    @Autowired
    public UserLogout(TokenService tokenService) {
        this.tokenService = tokenService;
    }
}
