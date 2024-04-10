package com.bbs.auth.app.follow;

import com.alibaba.fastjson.JSON;
import com.bbs.Result;
import com.bbs.auth.conf.RabbitmqConfig;
import com.bbs.auth.service.FanService;
import com.bbs.auth.util.MQUtil;
import lombok.Data;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 关注
 */
@RestController
@RequestMapping()
public class Follow {

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


    }

    /**
     * 添加关注
     */
    @PutMapping("/follow")
    public Result<Boolean> follow(@RequestBody @Valid Param param){
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            fanService.create(param);
            //发通知
            mq.send(
                    RabbitmqConfig.EXCHANGE_TOPICS_CHAT_INFORM,
                    RabbitmqConfig.ROUTINGKEY_FOLLOW,
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
