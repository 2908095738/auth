package com.bbs.auth.app.follow;

import com.alibaba.fastjson.JSON;
import com.bbs.Result;
import com.bbs.auth.conf.RabbitmqConfig;
import com.bbs.auth.service.FanService;
import com.bbs.auth.util.MQUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

@Slf4j
@RestController
public class DelFollow {

    @Resource
    private MQUtil mq;
    @Resource
    private FanService fanService;
    @Resource
    private TransactionDefinition transactionDefinition;
    @Resource
    private DataSourceTransactionManager transactionManager;

    @Data
    public static class Param {

        /**
         * 账号id
         */
        private Long userId;

        /**
         * 关注账号id
         */
        private Long followUserId;

        /**
         * 删除状态：0未删除  1已删除
         */
        private Integer deleteFlag = 1;

    }

    /**
     * 删除关注
     */
    @DeleteMapping("/follow")
    public Result<Boolean> del(@RequestBody @Valid Param param){
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            fanService.delFollow(param);
            //发通知
            mq.send(
                    RabbitmqConfig.EXCHANGE_TOPICS_CHAT_INFORM,
                    RabbitmqConfig.ROUTINGKEY_UNFOLLOW,
                    JSON.toJSONString(new com.bbs.auth.event.mq.Follow(param))
            );
            transactionManager.commit(transaction);
            return Result.success();
        } catch (RuntimeException e) {
            transactionManager.rollback(transaction);
            e.printStackTrace();
        }
        return Result.failedNull();
    }
}
