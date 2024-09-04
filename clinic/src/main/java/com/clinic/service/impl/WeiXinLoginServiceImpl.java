package com.clinic.service.impl;


import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.bbs.exception.BusinessException;
import com.clinic.service.AdmissionLogService;
import com.clinic.service.WeiXinLoginService;
import com.clinic.util.RedisUtil;
import com.clinic.util.WxUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class WeiXinLoginServiceImpl implements WeiXinLoginService {

    @Value("${vx.token}")
    private String token;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private WxUtil wxUtil;

    @Resource
    private AdmissionLogService admissionLogService;


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
        } catch (HttpException e) {
            e.printStackTrace();
            throw new BusinessException("获取tikect异常");
        }
        redisUtil.set("WX:"+ticket, "1"+","+phone, Long.parseLong(expireSeconds));
        // 通过ticket换取二维码 https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=
        HashMap<String, String> map = new HashMap<>();
        map.put("ticket", ticket);
        map.put("expire_seconds", expireSeconds);
        log.info("getQrCode方法执行结束！");
        return map;
    }




    /**
     * 验证当前手机号的病人是否扫码关注
     * @param ticket
     * @param phone
     * @return
     */
    @Override
    public Map<String, Object> checkPhone(String ticket, Long phone) {
        // 从缓存获取扫码状态
        String wxUser;
        String[] openUser;
        String openId;
        try {
            wxUser = redisUtil.get("WX:"+ticket);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        log.debug("redis中的openid：{}，1表达没有扫码或没有回调或没有关注关注号",wxUser);
        // 判断扫码状态
        if (StrUtil.isEmpty(ticket)){
            throw new BusinessException(phone+"WX:"+ticket+"的值为空");
        }
        openUser = wxUser.split(",");
        openId = openUser[0];

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
        HashMap<String, Object> scanResultMap3 = new HashMap<>();
        SymmetricCrypto aes = new SymmetricCrypto(SymmetricAlgorithm.AES, token.getBytes());
        scanResultMap3.put("openId", aes.encrypt(openId));
        scanResultMap3.put("scanResult",1);
        redisUtil.set("PATIENT:"+phone, openId);
        return scanResultMap3;
    }


}