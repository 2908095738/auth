package com.bbs.auth.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.nacos.common.utils.MD5Utils;
import com.bbs.auth.entity.User;
import com.bbs.auth.service.TokenService;
import com.bbs.auth.service.UserService;
import com.bbs.auth.service.WeiXinLoginService;
import com.bbs.auth.util.RedisUtil;
import com.bbs.auth.util.VxUtil;
import com.bbs.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class WeiXinLoginServiceImpl implements WeiXinLoginService {

    @Value("${vx.token}")
    private String token;
    //模板消息ID
    private static final String loginTemplateId = "30_oq_2GlEMfPkUunUk2HzXdbwF04aO1hjwMwymxA5Q";

    @Resource
    private UserService usetService;  //用户

    @Resource
    private TokenService tokenService;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private VxUtil wxUtil;
//
//    @Resource
//    private WxMpService wxMpService;



    @Override
    public Map<String,String> getQrCode() {
        log.info("getQrCode方法开始执行！");
        // 获取 AccessToken
        String accessToken = null;
        try {
            accessToken = wxUtil.getAccessToken();
            log.info("获取到的acesstoken为：‘{}’",accessToken);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("获取AccessToken异常");
        }
        // 获取ticket
        String ticket = null;
        String expireSeconds = null;
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
            log.info("请求微信接口的结果:'{}'",result);
            // 结果处理
            JSONObject ticketJson = JSONObject.parseObject(result);
            ticket = ticketJson.getString("ticket");
            expireSeconds = ticketJson.getString("expire_seconds");
        } catch (HttpException e) {
            e.printStackTrace();
            throw new BusinessException("获取tikect异常");
        }

        redisUtil.set("WEI_XIN_TICKET" + ticket, "1", Long.parseLong(expireSeconds));
        // 通过ticket换取二维码 https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=
        HashMap<String, String> map = new HashMap<>();
        map.put("ticket", ticket);
        map.put("expire_seconds", expireSeconds);
        log.info("getQrCode方法执行结束！");
        return map;
    }

    @Override
    public String receive(String signature, String timestamp, String nonce, String echostr, HttpServletRequest request) throws IOException {
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
            String ticket = resXml.get("Ticket"); // 获取二维码凭证
            String fromUserName = resXml.get("FromUserName"); // 获取OpenId
            String event = resXml.get("Event"); // 获取事件类型
            // 只处理带场景值的二维码事件推送
            if(StrUtil.isEmpty(ticket)){
                return "";
            }
            // 处理绑定微信号事件
            if ("1".equals(redisUtil.get("WEI_XIN_TICKET"+ticket))){
                //先删除
                redisUtil.delete("WEI_XIN_TICKET"+ticket);
                redisUtil.set("WEI_XIN_TICKET"+ticket,fromUserName,100000L);
            }
            //发送关注通知
            //数据库去查找是否存在该openId,如果没有就新创建一个新用户设置一下基本信息
//            Integer count = usetService.countByOpenId(fromUserName);
//            //说明数据库中没有这个人，创建一个新用户
//            if (!StrUtil.isEmpty(ticket) && count == 0){
//
//                //获取微信用户信息
//                log.info("获取到的用户信息为：{}",user);
//                String username = "U-"+fromUserName.substring(fromUserName.length() - 6);
//                User sysUser = new User();
//                sysUser.setName(user.getNickname());
//                sysUser.setOpenId(user.getOpenId());
//                sysUser.setSign("科技改变生活");
//                sysUser.setEmail(username+"@qq.com");
//                //上传图像
//                sysUser.setAvatar(user.getHeadImgUrl());
//                //保存用户
//                usetService.save(sysUser);
//            }
        }catch (Exception e){
            e.printStackTrace();
            throw new BusinessException("系统异常");
        }
        log.info("微信回调方法执行结束！");
        return "";
    }


    @Override
    public Map<String, Object> checkLogin(String ticket){
        log.info("checkLogin方法开始执行，入参ticket：{}",ticket);
        // 从缓存获取扫码状态
        String openId = null;
        try {
            openId = redisUtil.get("WEI_XIN_TICKET" + ticket);
            System.out.println(openId+"哈哈");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        log.info(openId);
        // 判断扫码状态
        if (StrUtil.isEmpty(ticket)){
            throw new BusinessException("WEI_XIN_TICKET"+ticket+"的值为空");
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
        User dbUser = usetService.searchIdByOpenId(openId);
        log.info("checkLogin方法执行结束！");
        // 判断用户是否存在
        if (dbUser == null){
            HashMap<String, Object> scanResultMap3 = new HashMap<>();
            SymmetricCrypto aes = new SymmetricCrypto(SymmetricAlgorithm.AES, token.getBytes());
            scanResultMap3.put("openId", aes.encrypt(openId));
            scanResultMap3.put("scanResult",0);
            return scanResultMap3;
        }
        //生成token
        String token = tokenService.createToken(dbUser);
        HashMap<String, Object> resultMap = new HashMap<>();

        resultMap.put("token", token);
        resultMap.put("user", dbUser);
        resultMap.put("scanResult",1);
        //公众号下发登录成功
        wxUtil.sendLoginMassage(openId, dbUser, loginTemplateId);
        return resultMap;
    }
}