package com.bbs.app.login;

import com.bbs.app.register.RegisterUser;
import com.bbs.cache.UserCache;
import com.bbs.converter.UserConverter;
import com.bbs.entity.User;
import com.bbs.entity.UserVO;
import com.bbs.enums.ResourceNames;
import com.bbs.service.ResourceService;
import com.bbs.service.TokenService;
import com.bbs.service.UserService;
import com.bbs.Result;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static com.bbs.Result.failed;
import static com.bbs.Result.success;
import static com.bbs.enums.CodeEnum.SUCCESS_USER_LOGIN;
import static java.util.Objects.nonNull;

@RestController
@RequestMapping
public class Email {

    private final UserCache cache;

    private final TokenService tokenService;

    private final UserConverter converter;

    private final UserService service;

    @Lazy
    @Resource
    private RegisterUser registerUser;

    @Resource
    private ResourceService resourceService;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class UserLoginParam {

        private String email;

        private String password;
    }

    @PostMapping("/user")
    public Result<UserVO> emailLogin(@RequestBody UserLoginParam param) throws InterruptedException, IllegalArgumentException {
        User user = cache.search(param.getEmail());
        if(!service.userIsValidity(user)) {
            return failed(400, "账号已过期！");
        }
        if(!service.userStateIsNormal(user)) {
            return failed(400, "账号状态不可用，请联系客服了解详情！");
        }
        if(verifyPassword(param.getPassword(), user)){
            tokenService.setLoginFlag(user.getId());
            UserVO vo = converter.toVO(user);

            // 设置诊所名称
            com.bbs.entity.Resource clinicNameConfig = resourceService.searchUserConfig(vo.getId(), ResourceNames.UserConfig.CLINIC_NAME.getName());
            if(nonNull(clinicNameConfig)) {
                vo.setClinicName(clinicNameConfig.getValue());
            }

            vo.setToken(tokenService.createToken(user));
            return success(SUCCESS_USER_LOGIN, vo);
        }
        return failed(400, "邮箱或密码错误!");
    }

    public Boolean verifyPassword(String inputPassword, User user) {
        String password = registerUser.encryptPassword(inputPassword, user.getSalt());
        return user.getPassword().equals(password);
    }

    @Autowired
    public Email(UserCache cache, TokenService tokenService, UserConverter converter, UserService service) {
        this.cache = cache;
        this.tokenService = tokenService;
        this.converter = converter;
        this.service = service;
    }
}
