package com.bbs.auth.app.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.auth.app.login.util.Util;
import com.bbs.auth.converter.UserConverter;
import com.bbs.auth.service.TokenService;
import com.bbs.auth.service.UserService;
import com.bbs.entity.UserVO;
import com.bbs.exception.ReLoginException;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import java.util.List;
import java.util.stream.Collectors;

import static com.bbs.Result.success;
import static java.util.Objects.nonNull;

@RestController
@RequestMapping
public class User {


    @Resource
    private TokenService tokenService;

    @Resource
    private UserService service;

    @Resource
    private UserConverter converter;

    /**
     * 当前用户个人信息
     */
    @GetMapping
    public Result<UserVO> currentUserInfo(HttpServletRequest request) throws ReLoginException {
        String token = tokenService.getToken(request);
        UserVO vo = tokenService.verify(token);
        return success(converter.toVO(service.search(vo.getId())));
    }

    @GetMapping("/list")
    public Result<Page<UserVO>> searchUser(
            @NotEmpty @RequestParam("val") String val,
            @NotNull @RequestParam("current") Integer current,
            @NotNull @RequestParam("size") Integer size
    ) throws ReLoginException {
        Long numVal = null;
        if(Util.isNumber(val)) {
            numVal = Long.parseLong(val);
        }
        Page<com.bbs.auth.entity.User> page = service.lambdaQuery()
                .like(!Util.isNumber(val), com.bbs.auth.entity.User::getName, val)
                .eq(nonNull(numVal), com.bbs.auth.entity.User::getPhone, numVal)
                .or()
                .eq(nonNull(numVal), com.bbs.auth.entity.User::getId, numVal)
                .page(new Page<>(current, size));
        List<UserVO> vos = page.getRecords().stream().map(converter::toVO).collect(Collectors.toList());
        Page<UserVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        return success(result.setRecords(vos));
    }
}
