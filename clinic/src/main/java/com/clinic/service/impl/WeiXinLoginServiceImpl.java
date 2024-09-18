package com.clinic.service.impl;


import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import com.bbs.exception.BusinessException;
import com.clinic.entity.AdmissionLog;
import com.clinic.entity.Patient;
import com.clinic.service.AdmissionLogService;
import com.clinic.service.PatientService;
import com.clinic.service.WeiXinLoginService;
import com.clinic.util.RedisUtil;
import com.clinic.util.WxUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class WeiXinLoginServiceImpl implements WeiXinLoginService {

    @Value("${wx.oa.token}")
    private String token;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private WxUtil wxUtil;

    @Resource
    private PatientService patientService;
    @Resource
    private AdmissionLogService admissionLogService;


    /**
     * 验证当前手机号的病人是否扫码关注
     * @param ticket
     * @param phone
     * @return
     */
    @Override
    public Map<String, Object> checkPhone(String ticket, Long phone, boolean isEnd) {
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

        Patient patient;
        if(isEnd){
            AdmissionLog dbOne = admissionLogService.getById(phone);
            patient = patientService.getById(dbOne.getPatientId());
            dbOne.setOpenId(openId);
            patient.setOpenId(openId);
            patientService.updateById(patient);
            admissionLogService.lambdaUpdate().set(AdmissionLog::getOpenId, openId).eq(AdmissionLog::getId, dbOne.getId()).update();
            //发送绑定成功消息
            wxUtil.sendPatientMassage(patient);
            //发送最近一次就诊处方记录
        }else{
            patient = patientService.selectByPhone(String.valueOf(phone));
            if(Objects.isNull(patient)){
                //病人还未创建完，暂时存到redis
                redisUtil.set("PATIENT:"+phone, openId);
            }else{
                patient.setOpenId(openId);
                patientService.updateById(patient);
                //发送绑定成功消息
                wxUtil.sendPatientMassage(patient);
            }
        }
        return scanResultMap3;
    }


}