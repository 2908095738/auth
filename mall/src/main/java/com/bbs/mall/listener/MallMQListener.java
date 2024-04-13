package com.bbs.mall.listener;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.bbs.Result;
import com.bbs.mall.entity.Cart;
import com.bbs.mall.mq.RabbitmqConfig;
import com.bbs.mall.service.CartService;
import com.bbs.mall.service.OrderService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

//TODO 下述监听当接收非200的响应时暂未进行处理
@Component
public class MallMQListener {

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @RabbitListener(queues = RabbitmqConfig.QUEUE_DEAD_C)
    public void receiveCartAdd(Message message, @Headers Map<String, Object> header, Channel channel) {
        Cart cart = JSON.parseObject((String) message.getPayload()).to(Cart.class);
        Result result = cartService.create(cart);
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

    @RabbitListener(queues = RabbitmqConfig.QUEUE_D_CART_UPD_C)
    public void cartUpdCache(Message message, @Headers Map<String, Object> header, Channel channel) {
        JSONObject jObj = JSON.parseObject((String) message.getPayload());
        Long userId = (Long) jObj.get("userId");
        Long prodId = (Long) jObj.get("prodId");

        Result result = cartService.updateAttr(userId, prodId);
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

    @RabbitListener(queues = RabbitmqConfig.QUEUE_D_ORDER_CANCEL)
    public void orderCancel(Message message, @Headers Map<String, Object> header, Channel channel) {
        List<String> orderSNs = JSON.parseArray((String) message.getPayload(), String.class);

        Result result = orderService.cancelOrder(orderSNs);
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