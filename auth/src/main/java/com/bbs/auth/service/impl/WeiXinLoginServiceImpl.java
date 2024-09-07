package com.bbs.auth.service.impl;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.bbs.auth.cache.user.UserCache;
import com.bbs.auth.entity.User;
import com.bbs.auth.entity.UserCompany;
import com.bbs.auth.service.CompanyService;
import com.bbs.auth.service.TokenService;
import com.bbs.auth.service.UserService;
import com.bbs.auth.service.WeiXinLoginService;
import com.bbs.auth.util.RedisUtil;
import com.bbs.auth.util.WxUtil;
import com.bbs.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.isNull;

@Service
@Slf4j
public class WeiXinLoginServiceImpl implements WeiXinLoginService {

    @Value("${vx.token}")
    private String token;

    @Resource
    private UserService userService;  //用户

    @Resource
    private UserCache userCache;

    @Resource
    private TokenService tokenService;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private WxUtil wxUtil;
    @Resource
    private CompanyService companyService;



    @Override
    public Map<String,String> getQrCode(Long phone) {
        log.info("getQrCode方法开始执行！");
        // 获取 AccessToken
        String accessToken;
        try {
            accessToken = wxUtil.getAccessToken();
            log.debug("获取到的acesstoken为：‘{}’",accessToken);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("获取AccessToken异常");
        }
        // 获取ticket
        String ticket;
        String expireSeconds;
        try {
            String url = "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=" + accessToken;
            // 组织请求数据
            Map<String, Object> jsonData = new HashMap<>();
            jsonData.put("expire_seconds", 1800); // 二维码过期时间
            jsonData.put("action_name", "QR_SCENE");
            Map<String, Object> actionInfo = new HashMap<>();
            Map<String, Object> scene = new HashMap<>();
            scene.put("scene_str", "3D");
            actionInfo.put("scene", scene);
            jsonData.put("action_info", actionInfo);
            // 发送请求
            String result = HttpRequest.post(url).body(JSON.toJSONString(jsonData)).execute().body();
            log.debug("请求微信接口的结果:'{}'",result);
            // 结果处理
            JSONObject ticketJson = JSONObject.parseObject(result);
            ticket = ticketJson.getString("ticket");
            expireSeconds = ticketJson.getString("expire_seconds");

            if(Objects.nonNull(phone)){
                redisUtil.set("WX:"+ticket, "1,"+phone, Long.parseLong(expireSeconds));
            }else{
                redisUtil.set("WX:"+ticket, "1", Long.parseLong(expireSeconds));
            }
            // 通过ticket换取二维码 https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=
            HashMap<String, String> map = new HashMap<>();
            map.put("ticket", ticket);
            map.put("expire_seconds", expireSeconds);
            log.info("getQrCode方法执行结束！");
            return map;
        } catch (HttpException e) {
            e.printStackTrace();
            throw new BusinessException("获取tikect异常");
        }
    }

    @Override
    public String receive(String signature, String timestamp, String nonce, String echostr, HttpServletRequest request) throws IOException {
        String xml = "success";
        log.info("微信回调方法开始执行");
        // 验证微信签名
        if (!wxUtil.checkSignature(signature, timestamp, nonce, token)){
            throw new BusinessException("微信回调参数异常！");
        }
        // 验证服务端配置
        if (echostr != null){
            return echostr;
        }
        // 接收微信推送的消息
        String xmlString = wxUtil.readRequest(request);
        try{
            Map<String, String> resXml = wxUtil.ResponseXmlToMap(xmlString);

            String fromUserName = resXml.get("FromUserName"); // 获取OpenId
            String toUserName = resXml.get("ToUserName");//开发者微信号
            String msgType = resXml.get("MsgType");//消息类型，event
            if (msgType.equals("event")) {
                String event = resXml.get("Event");
                String ticket = resXml.get("Ticket"); // 获取二维码凭证

                String user;
                String[] userArray;
                switch (event){
                    case "subscribe": //扫描带参数二维码事件-未关注
                        if(StrUtil.isEmpty(ticket)){
                            return xml;
                        }
                        log.debug("处理“扫描带参数二维码事件-未关注”事件");
                        // 处理绑定微信号事件
                        user = redisUtil.get("WX:" + ticket);
                        userArray = user.split(",");
                        if ("1".equals(userArray[0])){
                            //先删除
                            redisUtil.delete("WX:"+ticket);
                            log.debug("删除redis中的openid：{}",userArray[0]);
                            if(userArray.length>1&&ObjUtil.isNotEmpty(userArray[1])){
                                redisUtil.set("WX:"+ticket, fromUserName+","+userArray[1],100000L);
                            }else{
                                redisUtil.set("WX:"+ticket, fromUserName,100000L);
                            }
                        }
                        xml ="<xml>\n" +
                                "  <ToUserName><![CDATA[" + fromUserName + "]]></ToUserName>\n" +
                                "  <FromUserName><![CDATA[" + toUserName + "]]></FromUserName>\n" +
                                "  <CreateTime> "+ System.currentTimeMillis() + "</CreateTime>\n" +
                                "  <MsgType><![CDATA[text]]></MsgType>\n" +
                                "  <Content><![CDATA[科技改变生活！关注成功]]></Content>\n" +
                                "</xml>>\n";
                        break;
                    case "unsubscribe": //取消关注
                        break;
                    case "SCAN":// 扫描带参数二维码事件-已关注
                        if(StrUtil.isEmpty(ticket)){
                            return xml;
                        }
                        log.debug("处理“扫描带参数二维码事件-已关注”事件");
                        // 处理绑定微信号事件
                        user = redisUtil.get("WX:" + ticket);
                        userArray = user.split(",");
                        if ("1".equals(userArray[0])){
                            //先删除
                            redisUtil.delete("WX:"+ticket);
                            log.debug("1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111删除redis中的openid：{}",userArray[0]);
                            if(userArray.length>1&&ObjUtil.isNotEmpty(userArray[1])){
                                redisUtil.set("WX:"+ticket, fromUserName+","+userArray[1],100000L);
                            }else{
                                redisUtil.set("WX:"+ticket, fromUserName,100000L);
                            }
                        }
                        xml ="<xml>\n" +
                                "  <ToUserName><![CDATA[" + fromUserName + "]]></ToUserName>\n" +
                                "  <FromUserName><![CDATA[" + toUserName + "]]></FromUserName>\n" +
                                "  <CreateTime>" + System.currentTimeMillis() + "</CreateTime>\n" +
                                "  <MsgType><![CDATA[text]]></MsgType>\n" +
                                "  <Content><![CDATA[扫码成功!]]></Content>\n" +
                                "</xml>\n";
                        break;
                    case "CLICK":   //自定义菜单事件

                    default:
                        break;
                }
            }else if (msgType.equals("text")) {
                String Content = resXml.get("Content");//文本消息内容
                String MsgId = resXml.get("MsgId");//消息id，64位整型
                log.debug(Content);
            }
        }catch (Exception e){
            log.error("微信回调方法执行异常！{}",e.getMessage());
            e.printStackTrace();
            throw new BusinessException("系统异常");
        }
        log.info("微信回调方法执行结束！");
        return xml;
    }


    @Override
    public Map<String, Object> checkLogin(String ticket, Integer expireNumber){
        log.info("checkLogin方法开始执行，入参ticket：{}",ticket);
        // 从缓存获取扫码状态
        String openId;
        try {
            openId = redisUtil.get("WX:"+ticket);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        log.debug("redis中的openid：{}，1表达没有扫码或没有回调或没有关注关注号",openId);
        // 判断扫码状态
        if (StrUtil.isEmpty(ticket)){
            throw new BusinessException("WX:"+ticket+"的值为空");
        }
        if (openId == null){
            //说明二维码过期了，停止轮询
            HashMap<String, Object> scanResultMap2 = new HashMap<>();
            scanResultMap2.put("scanResult",-2);
            return scanResultMap2;
        }
        if(openId.equals("1")){
            //1表达没有回调，没有关注关注号
            HashMap<String, Object> scanResultMap = new HashMap<>();
            scanResultMap.put("scanResult",-1);
            return scanResultMap;
        }
        User dbUser = userService.searchIdByOpenId(openId);
        // 判断用户是否存在
        if (isNull(dbUser) || isNull(dbUser.getPhone())){
            HashMap<String, Object> scanResultMap3 = new HashMap<>();
            SymmetricCrypto aes = new SymmetricCrypto(SymmetricAlgorithm.AES, token.getBytes());
            scanResultMap3.put("openId", aes.encrypt(openId));
            scanResultMap3.put("scanResult",0);
            return scanResultMap3;
        }
        //生成token
        String token = tokenService.createToken(dbUser);
        HashMap<String, Object> resultMap = new HashMap<>();

        tokenService.setLoginFlag(dbUser.getId(), expireNumber, TimeUnit.DAYS);
        userCache.expireUserAndPhoneMap(dbUser);

        List<UserCompany> userCompanyList = companyService.searchCompany(dbUser.getId());

        resultMap.put("token", token);
        resultMap.put("user", dbUser);
        resultMap.put("scanResult",1);
        resultMap.put("userCompanyList", userCompanyList);
        //公众号下发登录成功
        wxUtil.sendLoginMassage(openId, dbUser);
        log.info("checkLogin方法执行结束！");
        return resultMap;
    }
}