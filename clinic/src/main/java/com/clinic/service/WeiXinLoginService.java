package com.clinic.service;

import java.util.Map;

public interface WeiXinLoginService {

    Map<String,String> getQrCode(Long phone);

    Map<String, Object> checkPhone(String ticket, Long phone);
}