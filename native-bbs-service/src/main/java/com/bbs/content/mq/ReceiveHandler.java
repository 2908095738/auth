package com.bbs.content.mq;

import com.alibaba.fastjson.JSON;
import com.bbs.content.service.UserAccountService;
import com.bbs.content.util.AuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class ReceiveHandler {

    private UserAccountService userAccountService;

//    监听队列，注册用户
    @RabbitListener(queues = {RabbitmqConfig.USER_REGISTER_EVENT_QUEUE})
    public void receive_agree(Message message){
        AuthUtil.UserAPI.User user = JSON.parseObject(message.getBody(), AuthUtil.UserAPI.User.class);
        userAccountService.create(user);
    }

    @Resource
    public void setUserAccountService(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }
}