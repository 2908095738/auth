package com.bbs.mall.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bbs.Result;
import com.bbs.mall.dto.OrderConfirmDto;
import com.bbs.mall.dto.OrderDetailDto;
import com.bbs.mall.dto.OrderDto;
import com.bbs.mall.dto.param.OrderParam;
import com.bbs.mall.entity.Order;
import io.swagger.annotations.ApiParam;

import java.util.List;

public interface OrderService extends IService<Order> {
    /**
     * 获取商品确认单
     *
     * @param userId  用户id
     * @param prodIds 商品id列表
     */
    OrderConfirmDto getConfirmOrder(Long userId, List<Long> prodIds);

    /**
     * 创建订单
     *
     * @param param  订单创建参数
     * @param userId 用户id
     * @return
     */
    Result createOrder(OrderParam param, Long userId);

    /**
     * 取消订单
     *
     * @param orderSNs 订单SN码列表
     * @return
     */
    Result cancelOrder(List<String> orderSNs);

    //TODO 定时器调用
    Result cancel2LongOrder();

    /**
     * 支付成功回调
     *
     * @param orderSN 订单SN码
     * @param payType 支付方式：0->未支付；1->支付宝；2->微信
     * @return
     */
    Result paySuccess(String orderSN, Integer payType);

    /**
     * 分页获取订单列表
     * //TODO 目前泛型没限制，暂以返回数据为核心
     *
     * @param userId  用户id
     * @param status  订单状态：-1->全部；0->待付款；1->待发货；2->已发货；3->已完成；4->已关闭
     * @param current 页码
     * @param size    条数
     * @return
     */
    Result<Page> list(Long userId, Integer status, Integer current, Integer size);

    /**
     * 获取订单详情
     *
     * @param orderId 订单id
     * @return
     */
    Result<OrderDetailDto> detail(Long orderId);
}