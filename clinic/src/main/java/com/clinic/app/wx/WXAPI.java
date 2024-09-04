package com.clinic.app.wx;

import com.bbs.Result;
import com.clinic.service.WeiXinLoginService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@Slf4j
public class WXAPI {

    @Resource
    private WeiXinLoginService weiXinLoginService;



    @ApiOperation("微信扫码登录，提供二维码")
    @PostMapping(value = "/weixin/getQRCode")
    public Result<Map<String, String>> weinLogin(Long phone){
        log.debug("微信扫码关注接口开始执行：/weixin/getQRCode");
        //获取ticket
        Map<String, String> codeResult = weiXinLoginService.getQrCode(phone);
        log.debug("微信扫码登录接口执行结束！");
        return Result.success(codeResult);
    }

    @GetMapping("/weixin/checkPhone")
    @ApiOperation("验证当前手机号的病人是否扫码关注")
    public Result<Map<String, Object>> checkPhone(@RequestParam String ticket, @RequestParam Long phone, @RequestParam Long timestamp) {
        log.debug("前端二维码轮询接口开始执行/weixin/check,timestamp{}",timestamp);
        Map<String, Object> resultMap = weiXinLoginService.checkPhone(ticket, phone);
        log.debug("前端二维码轮询接口执行结束！");
        return Result.success(resultMap);
    }





}
