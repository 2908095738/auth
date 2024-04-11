package com.bbs.content.controller;


import com.alibaba.fastjson.JSON;
import com.bbs.Result;
import com.bbs.content.cache.FileCache;
import com.bbs.content.dto.AuditNewDto;
import com.bbs.content.dto.MqAuditStatusDto;
import com.bbs.content.enums.NewCommentStatus;
import com.bbs.content.mq.RabbitmqConfig;
import com.bbs.content.mq.RabbitmqSend;
import com.bbs.content.service.NewsService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminManageController {

    private NewsService newsService;

    private FileCache fileCache;

    private RabbitmqSend rabbitmqSend;


    /**
     * 查询待审核内容+视频+图片
     */
    @GetMapping("/content")
    public Result<List<AuditNewDto>> getWaitAudit(){
        return Result.success(fileCache.getAuditFile());
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class PutAuditStatusParam{
        private Integer status;
        private List<String> fileLocalPathList;
        private Long newId;
        private Long userId;
//        private String remark;
    }

    /**
     * 审核：通过；不通过+原因
     */
    @PostMapping("/content")
    public Result<Boolean> putAuditStatus(@RequestBody PutAuditStatusParam param){
        //删除文件
        fileCache.delAuditFiles(param.getFileLocalPathList(),param.getNewId());
        //修改状态
        newsService.updateStatus(param.getStatus(),param.getNewId());
        //发通知
        if(param.getStatus().equals(NewCommentStatus.HAVE_RELEASED.getCode())){
            rabbitmqSend.send(RabbitmqConfig.EXCHANGE_TOPICS_CHAT_INFORM,RabbitmqConfig.ROUTINGKEY_PASS_AUDIT, JSON.toJSONString(new MqAuditStatusDto(param.newId,param.userId, new Date())));
        }{
            rabbitmqSend.send(RabbitmqConfig.EXCHANGE_TOPICS_CHAT_INFORM,RabbitmqConfig.ROUTINGKEY_NO_PASS_AUDIT, JSON.toJSONString(new MqAuditStatusDto(param.newId,param.userId, new Date())));
        }
        return Result.success();
    }

    /**
     * 删除评论
     */








    @Autowired
    public AdminManageController(NewsService newsService, FileCache fileCache, RabbitmqSend rabbitmqSend) {
        this.newsService = newsService;
        this.fileCache = fileCache;
        this.rabbitmqSend = rabbitmqSend;
    }
}
