package com.bbs.chat.listener;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.bbs.Result;
import com.bbs.chat.converter.CommentConverter;
import com.bbs.chat.converter.FavoritesConverter;
import com.bbs.chat.converter.FollowConverter;
import com.bbs.chat.converter.ThumbConverter;
import com.bbs.chat.dto.MqCommentDto;
import com.bbs.chat.dto.MqFavoritesDto;
import com.bbs.chat.dto.MqFollowDto;
import com.bbs.chat.dto.param.CancelThumbParam;
import com.bbs.chat.dto.param.CreateThumbParam;
import com.bbs.chat.entity.Comment;
import com.bbs.chat.entity.Fan;
import com.bbs.chat.mq.RabbitmqConfig;
import com.bbs.chat.service.CommentService;
import com.bbs.chat.service.FavoritesService;
import com.bbs.chat.service.FollowService;
import com.bbs.chat.service.ThumbService;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

//TODO 下述监听当接收非200的响应时暂未进行处理
@Component
public class ChatMQListener {
    private static final Logger log = LoggerFactory.getLogger(ChatMQListener.class);
    @Autowired
    private ThumbService thumbService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private FollowService followService;

    @Autowired
    private FavoritesService favoritesService;

    @Autowired
    private ThumbConverter thumbConverter;

    @Autowired
    private FollowConverter followConverter;

    @Autowired
    private CommentConverter commentConverter;

    @Autowired
    private FavoritesConverter favoritesConverter;

//    @RabbitListener(queues = RabbitmqConfig.QUEUE_INFORM_AGREE)
    public void receiveThumb(Message message, @Headers Map<String, Object> header, Channel channel) {
        log.error("message"+message.getPayload());
        CreateThumbParam createThumbParam = JSON.parseObject((String) message.getPayload()).to(CreateThumbParam.class);
        Result result = thumbService.createThumb(createThumbParam);
        Long deliveryTag = (Long) header.get(AmqpHeaders.DELIVERY_TAG);
        try {
            if (result.getCode() == 200) {
                channel.basicAck(deliveryTag, false);//确认消费
            } else {
            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                channel.basicNack(deliveryTag, false, true);//回复nack,并重新发送
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

//    @RabbitListener(queues = RabbitmqConfig.QUEUE_INFORM_DEL_AGREE)
    public void receiveThumbCancel(Message message, @Headers Map<String, Object> header, Channel channel) {
        CancelThumbParam cancelThumbParam = JSON.parseObject((String) message.getPayload()).to(CancelThumbParam.class);
        Result result = thumbService.cancelThumb(cancelThumbParam);
        Long deliveryTag = (Long) header.get(AmqpHeaders.DELIVERY_TAG);
        try {
            if (result.getCode() == 200) {
                channel.basicAck(deliveryTag, false);//确认消费
            } else {
            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                channel.basicNack(deliveryTag, false, true);//回复nack,并重新发送
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

//    @RabbitListener(queues = RabbitmqConfig.QUEUE_COMMENT)
    public void receiveComm(Message message, @Headers Map<String, Object> header, Channel channel) {
        //日期防报错特殊处理
        JSONObject tmpJSON = JSON.parseObject((String) message.getPayload());

        long timestamp = (long) tmpJSON.get("time");
        Date now = new Date(timestamp);

        //dto赋值
        tmpJSON.remove("time");
        MqCommentDto tmpDto = tmpJSON.to(MqCommentDto.class);
        Comment comment = commentConverter.toEntity(tmpDto);
        comment.setCreateTime(now);

        Result result = commentService.createComment(comment);
        Long deliveryTag = (Long) header.get(AmqpHeaders.DELIVERY_TAG);
        try {
            if (result.getCode() == 200) {
                channel.basicAck(deliveryTag, false);//确认消费
            } else {
            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                channel.basicNack(deliveryTag, false, true);//回复nack,并重新发送
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //TODO Cloud 队列-路由键错误
//    @RabbitListener(queues = RabbitmqConfig.QUEUE_FOLLOW)
    public void receiveFollow(Message message, @Headers Map<String, Object> header, Channel channel) {
        //日期防报错特殊处理
        JSONObject tmpJSON = JSON.parseObject((String) message.getPayload());
        tmpJSON.remove("createTime");

        //dto赋值
        MqFollowDto tmpDto = tmpJSON.to(MqFollowDto.class);
        Fan fan = followConverter.toEntity(tmpDto);
        fan.setCreateTime(new Date());

        Result result = followService.createFollow(fan);
        Long deliveryTag = (Long) header.get(AmqpHeaders.DELIVERY_TAG);
        try {
            if (result.getCode() == 200) {
                channel.basicAck(deliveryTag, false);//确认消费
            } else {
            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                channel.basicNack(deliveryTag, false, true);//回复nack,并重新发送
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

//    @RabbitListener(queues = RabbitmqConfig.QUEUE_UNFOLLOW)
    public void receiveUNFollow(Message message, @Headers Map<String, Object> header, Channel channel) {
        //日期防报错特殊处理
        JSONObject tmpJSON = JSON.parseObject((String) message.getPayload());
        tmpJSON.remove("createTime");

        //dto赋值
        MqFollowDto tmpDto = tmpJSON.to(MqFollowDto.class);
        Fan fan = followConverter.toEntity(tmpDto);
        fan.setCreateTime(new Date());

        Result result = followService.cancelFollow(fan);
        Long deliveryTag = (Long) header.get(AmqpHeaders.DELIVERY_TAG);
        try {
            if (result.getCode() == 200) {
                channel.basicAck(deliveryTag, false);//确认消费
            } else {
            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                channel.basicNack(deliveryTag, false, true);//回复nack,并重新发送
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

//    @RabbitListener(queues = RabbitmqConfig.QUEUE_FAVORITE)
    public void receiveFavo(Message message, @Headers Map<String, Object> header, Channel channel) {
        //防报错
        JSONObject tmpJSON = JSON.parseObject((String) message.getPayload());
        tmpJSON.remove("createTime");
        MqFavoritesDto tmpDto = tmpJSON.to(MqFavoritesDto.class);

        Result result = favoritesService.createFavorites(favoritesConverter.toEntity(tmpDto));
        Long deliveryTag = (Long) header.get(AmqpHeaders.DELIVERY_TAG);
        try {
            if (result.getCode() == 200) {
                channel.basicAck(deliveryTag, false);//确认消费
            } else {
            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                channel.basicNack(deliveryTag, false, true);//回复nack,并重新发送
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

//    @RabbitListener(queues = RabbitmqConfig.QUEUE_UNFAVORITE)
    public void receiveUNFavo(Message message, @Headers Map<String, Object> header, Channel channel) {
        JSONObject tmpJSON = JSON.parseObject((String) message.getPayload());
        MqFavoritesDto tmpDto = tmpJSON.to(MqFavoritesDto.class);

        Result result = favoritesService.cancelFavorites(favoritesConverter.toEntity(tmpDto));
        Long deliveryTag = (Long) header.get(AmqpHeaders.DELIVERY_TAG);
        try {
            if (result.getCode() == 200) {
                channel.basicAck(deliveryTag, false);//确认消费
            } else {
            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                channel.basicNack(deliveryTag, false, true);//回复nack,并重新发送
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}