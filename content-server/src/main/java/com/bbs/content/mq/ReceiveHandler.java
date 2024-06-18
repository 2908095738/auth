package com.bbs.content.mq;

import com.bbs.content.service.UserAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Lazy(value = false)
@Slf4j
public class ReceiveHandler {

    private UserAccountService userAccountService;

//    监听队列，注册用户
//    @RabbitListener(queues = {RabbitmqConfig.USER_REGISTER_EVENT_QUEUE})
//    public void receive(Message message){
//        AuthUtil.UserAPI.User user = JSON.parseObject(message.getBody(), AuthUtil.UserAPI.User.class);
//        userAccountService.create(user);
//    }


    @Resource
    public void setUserAccountService(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }
}