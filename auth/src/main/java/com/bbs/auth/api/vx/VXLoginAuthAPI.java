package com.bbs.auth.api.vx;

import com.bbs.Result;
import com.bbs.auth.service.WeiXinLoginService;
import com.bbs.auth.util.VxUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

@Api(tags = "微信登录相关接口")
@RestController
@Slf4j
public class VXLoginAuthAPI {

    @Resource
    private WeiXinLoginService weiXinLoginService;


    /**
     * 微信用户token认证
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @GetMapping(value = "/weixin/index")
    public String doGet(String signature,String timestamp, String nonce, String echostr)
            throws ServletException, IOException {

        // 接收微信服务器以Get请求发送的4个参数
//        String signature = request.getParameter("signature");
//        String timestamp = request.getParameter("timestamp");
//        String nonce = request.getParameter("nonce");
//        String echostr = request.getParameter("echostr");
//
//        PrintWriter out = response.getWriter();
        if (VxUtil.checkSignature(signature, timestamp, nonce)) {
            return echostr;        // 校验通过，原样返回echostr参数内容
        } else {
            System.out.println("不是微信发来的请求！");
        }
        return null;
    }



    @ApiOperation("微信扫码登录，提供二维码")
    @PostMapping(value = "/weixin/getQRCode")
    public Result weinLogin(){
        log.info("微信扫码登录接口开始执行：/weixin/getQRCode");
        //获取ticket
        Map<String, String> codeResult = weiXinLoginService.getQrCode();
        log.info("微信扫码登录接口执行结束！");
        return Result.success(codeResult);
    }


    @PostMapping(value = "/weixin/index")
    @ApiOperation("接收微信消息事件,判断用户是否完成扫码关注")
    public String postWxLoginReceive(HttpServletRequest request) throws IOException {
        log.info("微信回调接口开始执行post请求/weixin/receive");
        // 获取微信请求参数
        String signature = request.getParameter("signature");
        String timestamp = request.getParameter("timestamp");
        String nonce = request.getParameter("nonce");
        String echostr = request.getParameter("echostr");
        log.info("开始校验此次消息是否来自微信服务器，param->signature:{},\ntimestamp:{},\nnonce:{},\nechostr:{}",
                signature, timestamp, nonce, echostr);
        String result = weiXinLoginService.receive(signature,timestamp,nonce,echostr,request);
        System.out.println(result);
        log.info("微信回调接口post请求执行结束！");
        return result;
    }

    @GetMapping("/weixin/check")
    @ApiOperation("获取扫码登录状态,前端进行轮询")
    public Result checkLogin(@RequestParam String ticket) {
        log.info("前端二维码轮询接口开始执行/weixin/check");
        Map<String, Object> resultMap = weiXinLoginService.checkLogin(ticket);
        log.info("前端二维码轮询接口执行结束！");
        return Result.success(resultMap);
    }




}
