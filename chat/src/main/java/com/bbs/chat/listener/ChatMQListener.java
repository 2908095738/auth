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
import com.bbs.chat.mq.RabbitmqConfig;
import com.bbs.chat.service.CommentService;
import com.bbs.chat.service.FavoritesService;
import com.bbs.chat.service.FollowService;
import com.bbs.chat.service.ThumbService;
import com.rabbitmq.client.Channel;
import org.checkerframework.checker.units.qual.A;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
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

    @RabbitListener(queues = RabbitmqConfig.QUEUE_INFORM_AGREE)
    public void receiveThumb(Message message, @Headers Map<String, Object> header, Channel channel) {
        CreateThumbParam createThumbParam = JSON.parseObject((String) message.getPayload()).to(CreateThumbParam.class);
        Result result = thumbService.createThumb(thumbConverter.toEntity(createThumbParam));
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

    @RabbitListener(queues = RabbitmqConfig.QUEUE_INFORM_DEL_AGREE)
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

    @RabbitListener(queues = RabbitmqConfig.QUEUE_COMMENT)
    public void receiveComm(Message message, @Headers Map<String, Object> header, Channel channel) {
        MqCommentDto mqCommentDto = JSON.parseObject((String) message.getPayload()).to(MqCommentDto.class);
        Result result = commentService.createComment(commentConverter.toEntity(mqCommentDto));
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

    @RabbitListener(queues = RabbitmqConfig.QUEUE_FOLLOW)
    public void receiveFollow(Message message, @Headers Map<String, Object> header, Channel channel) {
        //日期防报错特殊处理
        JSONObject tmpJSON = JSON.parseObject((String) message.getPayload());
        long timestamp = (long) tmpJSON.get("createTime");
        Date now = new Date(timestamp);

        //dto赋值
        tmpJSON.remove("createTime");
        MqFollowDto tmpDto = tmpJSON.to(MqFollowDto.class);
        tmpDto.setCreateTime(now);

        Result result = followService.createFollow(followConverter.toEntity(tmpDto));
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

    @RabbitListener(queues = RabbitmqConfig.QUEUE_UNFOLLOW)
    public void receiveUNFollow(Message message, @Headers Map<String, Object> header, Channel channel) {
        //日期防报错特殊处理
        JSONObject tmpJSON = JSON.parseObject((String) message.getPayload());
        long timestamp = (long) tmpJSON.get("createTime");
        Date now = new Date(timestamp);

        //dto赋值
        tmpJSON.remove("createTime");
        MqFollowDto tmpDto = tmpJSON.to(MqFollowDto.class);
        tmpDto.setCreateTime(now);

        Result result = followService.cancelFollow(followConverter.toEntity(tmpDto));
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

    @RabbitListener(queues = RabbitmqConfig.QUEUE_FAVORITE)
    public void receiveFavo(Message message, @Headers Map<String, Object> header, Channel channel) {
        //日期防报错特殊处理
        JSONObject tmpJSON = JSON.parseObject((String) message.getPayload());
        long timestamp = (long) tmpJSON.get("createTime");
        Date now = new Date(timestamp);

        //dto赋值
        tmpJSON.remove("createTime");
        MqFavoritesDto tmpDto = tmpJSON.to(MqFavoritesDto.class);
        tmpDto.setCreateTime(now);

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

    @RabbitListener(queues = RabbitmqConfig.QUEUE_UNFAVORITE)
    public void receiveUNFavo(Message message, @Headers Map<String, Object> header, Channel channel) {
        //日期防报错特殊处理
        JSONObject tmpJSON = JSON.parseObject((String) message.getPayload());
        long timestamp = (long) tmpJSON.get("createTime");
        Date now = new Date(timestamp);

        //dto赋值
        tmpJSON.remove("createTime");
        MqFavoritesDto tmpDto = tmpJSON.to(MqFavoritesDto.class);
        tmpDto.setCreateTime(now);

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