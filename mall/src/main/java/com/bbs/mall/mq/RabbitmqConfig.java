package com.bbs.mall.mq;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitmqConfig {

    //Core Exchange
    public static final String EXCHANGE_DIRECT_MALL = "mall.direct";

    public static final String EXCHANGE_DIRECT_DEAD_M = "dead.mall.direct";

    //Cart-Create
    public static final String QUEUE_CART = "queue_cart";

    public static final String QUEUE_DEAD_C = "queue_dead_cart";

    //Cart-Update
    public static final String QUEUE_CART_UPD_C = "queue_cart_update_cache";

    public static final String QUEUE_D_CART_UPD_C = "queue_dead_cart_update_cache";

    //Order-Cancel
    public static final String QUEUE_ORDER_CANCEL = "queue_order_cancel";

    public static final String QUEUE_D_ORDER_CANCEL = "queue_dead_order_cancel";

    //Cart-Create-Key
    public static final String ROUTINGKEY_CART_A = "cart_add";

    public static final String ROUTINGKEY_DEAD_C = "dead_cart";

    //Cart-Update-Key
    public static final String KEY_CART_UPD_C = "cart_update_cache";
    public static final String KEY_DEAD_CART_UPD_C = "dead_cart_update_cache";

    //Order-Cancel-Key
    public static final String KEY_ORDER_CANCEL = "order_cancel";

    public static final String KEY_D_ORDER_CANCEL = "dead_order_cancel";

    //Core Exchange Init
    @Bean(EXCHANGE_DIRECT_MALL)
    public DirectExchange EXCHANGE_DIRECT_MALL() {
        return ExchangeBuilder.directExchange(EXCHANGE_DIRECT_MALL).durable(true).build();
    }

    @Bean(EXCHANGE_DIRECT_DEAD_M)
    public DirectExchange EXCHANGE_DIRECT_DEAD_M() {
        return ExchangeBuilder.directExchange(EXCHANGE_DIRECT_DEAD_M).durable(true).build();
    }

    //Normal Queue Init
    private static final int TMP_TTL = 2000;//TODO 测试延时消息的时间设置

    @Bean(QUEUE_CART)
    public Queue QUEUE_CART() {
        Map<String, Object> arguments = new HashMap(3);
        arguments.put("x-dead-letter-exchange", EXCHANGE_DIRECT_DEAD_M);
        arguments.put("x-dead-letter-routing-key", ROUTINGKEY_DEAD_C);
        arguments.put("x-message-ttl", TMP_TTL);

        return QueueBuilder.durable(QUEUE_CART).withArguments(arguments).build();
    }

    @Bean(QUEUE_CART_UPD_C)
    public Queue QUEUE_CART_UPD_C() {
        Map<String, Object> arguments = new HashMap(3);
        arguments.put("x-dead-letter-exchange", EXCHANGE_DIRECT_DEAD_M);
        arguments.put("x-dead-letter-routing-key", KEY_DEAD_CART_UPD_C);
        arguments.put("x-message-ttl", TMP_TTL);

        return QueueBuilder.durable(QUEUE_CART_UPD_C).withArguments(arguments).build();
    }

    @Bean(QUEUE_ORDER_CANCEL)
    public Queue QUEUE_ORDER_CANCEL() {
        Map<String, Object> arguments = new HashMap(3);
        arguments.put("x-dead-letter-exchange", EXCHANGE_DIRECT_DEAD_M);
        arguments.put("x-dead-letter-routing-key", KEY_D_ORDER_CANCEL);
        arguments.put("x-message-ttl", TMP_TTL);

        return QueueBuilder.durable(QUEUE_ORDER_CANCEL).withArguments(arguments).build();
    }

    //Dead Queue Init
    @Bean(QUEUE_DEAD_C)
    public Queue QUEUE_DEAD_C() {
        return QueueBuilder.durable(QUEUE_DEAD_C).build();
    }

    @Bean(QUEUE_D_CART_UPD_C)
    public Queue QUEUE_D_CART_UPD_C() {
        return QueueBuilder.durable(QUEUE_D_CART_UPD_C).build();
    }

    @Bean(QUEUE_D_ORDER_CANCEL)
    public Queue QUEUE_D_ORDER_CANCEL() {
        return QueueBuilder.durable(QUEUE_D_ORDER_CANCEL).build();
    }

    //Cart Bind
    @Bean
    public Binding queueBindNormalE(@Qualifier(QUEUE_CART) Queue queue, @Qualifier(EXCHANGE_DIRECT_MALL) Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTINGKEY_CART_A).noargs();
    }

    @Bean
    public Binding deadQBindDeadE(@Qualifier(QUEUE_DEAD_C) Queue deadQ, @Qualifier(EXCHANGE_DIRECT_DEAD_M) Exchange deadE) {
        return BindingBuilder.bind(deadQ).to(deadE).with(ROUTINGKEY_DEAD_C).noargs();
    }

    //Cart Cache Update Bing
    @Bean
    public Binding cartUpdCacheBing(@Qualifier(QUEUE_CART_UPD_C) Queue queue, @Qualifier(EXCHANGE_DIRECT_MALL) Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(KEY_CART_UPD_C).noargs();
    }

    @Bean
    public Binding dCartUpdCacheBind(@Qualifier(QUEUE_D_CART_UPD_C) Queue deadQ, @Qualifier(EXCHANGE_DIRECT_DEAD_M) Exchange deadE) {
        return BindingBuilder.bind(deadQ).to(deadE).with(KEY_DEAD_CART_UPD_C).noargs();
    }

    //Order Cancel Bing
    @Bean
    public Binding orderCancelBind(@Qualifier(QUEUE_ORDER_CANCEL) Queue queue, @Qualifier(EXCHANGE_DIRECT_MALL) Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(KEY_ORDER_CANCEL).noargs();
    }

    @Bean
    public Binding dOrderCancelBind(@Qualifier(QUEUE_D_ORDER_CANCEL) Queue deadQ, @Qualifier(EXCHANGE_DIRECT_DEAD_M) Exchange deadE) {
        return BindingBuilder.bind(deadQ).to(deadE).with(KEY_D_ORDER_CANCEL).noargs();
    }
}