package com.bbs.auth.app;

import com.bbs.auth.util.captcha.CaptchaUtil;
import com.bbs.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 短信验证码
 */
@Slf4j
@RestController
@RequestMapping
public class Code {

    @Resource
    private CaptchaUtil util;

    @GetMapping("/phone")
    public Result<Boolean> send(@RequestParam("phone") String phone) {
        return util.send(phone) ? Result.success() : Result.failed();
    }
}
