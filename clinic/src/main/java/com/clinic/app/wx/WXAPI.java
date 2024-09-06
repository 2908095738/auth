package com.clinic.app.wx;

import com.bbs.Result;
import com.clinic.service.WeiXinLoginService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@Slf4j
public class WXAPI {

    @Resource
    private WeiXinLoginService weiXinLoginService;


    @GetMapping("/weixin/checkPatient")
    @ApiOperation("验证当前手机号的病人是否扫码关注")
    public Result<Map<String, Object>> checkPhone(@RequestParam String ticket, @RequestParam Long phone,@RequestParam boolean isEnd, @RequestParam Long timestamp) {
        log.debug("前端二维码轮询接口开始执行/weixin/checkPatient,timestamp{}",timestamp);
        Map<String, Object> resultMap = weiXinLoginService.checkPhone(ticket, phone, isEnd);
        log.debug("前端二维码轮询接口执行结束！");
        return Result.success(resultMap);
    }





}
