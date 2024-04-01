package com.bbs.content.mq;

import cn.hutool.core.io.FileTypeUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.bbs.content.dto.FileDto;
import com.bbs.content.enums.RedisKeys;
import com.bbs.content.util.FileUtils;
import com.bbs.content.util.RedisUtil;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.bbs.content.util.FileUtils.fileType;

@Component
@Slf4j
public class ReceiveHandler {

    private RedisUtil redisUtil;

    /**
     * 监听队列,压缩文件
     */
    @RabbitListener(queues = {RabbitmqConfig.QUEUE_INFORM_WAIT_FILE})
    public void receive_bbs(Message message, @Headers Map<String, Object> header, Channel channel) {
        Long deliveryTag = (Long) header.get(AmqpHeaders.DELIVERY_TAG);
        try {
            FileDto fileDto = JSON.parseObject(message.getBody(), FileDto.class);
            MultipartFile file = fileDto.getFile();
            String filePath = fileDto.getFilePath();
            //文件的请求路径根据文件类型分类
            String type = FileTypeUtil.getType(file.getInputStream());
            if (fileType.get(type) == 1) {//图片
                //图片压缩落地
                file.transferTo(new File(filePath));
                FileUtils.doWithPhoto(filePath);
            } else if (fileType.get(type) == 2) {//视频
                //压缩落地
                FileUtils.compressionVideo(FileUtils.multipartFileToFile(file), filePath);
            }
            //放入redis
            putAuditRedis(RedisKeys.AUDIT_NEWS_USERID.key(),fileDto);
            //处理消息：手动回复ack:deliveryTag：从header中获取是哪一条消息（消息编号）,false：不批量回复
            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            e.printStackTrace();
            try {
                //回复nack,并重新发送
                channel.basicNack(deliveryTag, false, true);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void putAuditRedis(String key, FileDto fileDto) {
        String json = (String) redisUtil.hashGet(key+fileDto.getCreateId(),fileDto.getNewId().toString());
        Set<String> newFiles = StringUtils.isBlank(json)? new HashSet<>() : JSON.parseObject(json, new TypeReference<Set<String>>(){});

        newFiles.add(fileDto.getFilePath());
        redisUtil.hashSet(RedisKeys.AUDIT_NEWS_USERID.key()+fileDto.getCreateId(),fileDto.getNewId(),JSON.toJSONString(newFiles));
    }


    //监听队列
//    @RabbitListener(queues = {RabbitmqConfig.QUEUE_INFORM_AGREE})
//    public void receive_agree(Message message){
//
//        log.debug("message:{}",JSON.parseObject(message.getBody(), MqAgreeDto.class));
//
//    }

    @Resource
    public void setRedisUtil(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }
}