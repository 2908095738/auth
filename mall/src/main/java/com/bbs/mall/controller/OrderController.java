package com.bbs.mall.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.mall.dto.OrderConfirmDto;
import com.bbs.mall.dto.OrderDetailDto;
import com.bbs.mall.dto.param.OrderParam;
import com.bbs.mall.service.OrderService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Controller
@ApiOperation("订单管理")
@RequestMapping("/order")
public class OrderController {

    private OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @ResponseBody
    @ApiOperation("获取商品确认单")
    @GetMapping("/get/Confirm/Order")
    public Result<OrderConfirmDto> getConfirmOrder(@ApiParam("商品id列表") @RequestBody List<Long> prodIds) {
        //TODO 用户相关
//        AuthUtil.UserAPI.User currentUser = ThreadLocalUtil.getCurrentUser();
        Long userId = 1L;

        OrderConfirmDto dto = orderService.getConfirmOrder(userId, prodIds);
        return Result.success(dto);
    }

    @ResponseBody
    @ApiOperation("创建订单")
    @PostMapping("/create/Order")
    public Result createOrder(@ApiParam("订单创建参数") @RequestBody @Valid OrderParam param) {
        //TODO 用户相关
//        AuthUtil.UserAPI.User currentUser = ThreadLocalUtil.getCurrentUser();
        Long userId = 1L;

        return orderService.createOrder(param, userId);
    }

    @ResponseBody
    @ApiOperation("支付成功回调")
    @PostMapping("/paySuccess")
    public Result paySuccess(@ApiParam("订单SN码") String orderSN, @ApiParam("支付方式") Integer payType) {
        return orderService.paySuccess(orderSN, payType);
    }

    @ResponseBody
    @ApiOperation("分页获取订单列表")
    @ApiImplicitParam(name = "status", value = "订单状态：-1->全部；0->待付款；1->待发货；2->已发货；3->已完成；4->已关闭",
            allowableValues = "-1,0,1,2,3,4", paramType = "query", dataType = "int")
    @GetMapping("/list")
    public Result<Page> list(Integer status, @ApiParam("页码") Integer current, @ApiParam("条数") Integer size) {
        //TODO 用户相关
//        AuthUtil.UserAPI.User currentUser = ThreadLocalUtil.getCurrentUser();
        Long userId = 1L;

        //TODO 暂时分页不准，因为是从所有订单中分页，但后面又把取出的订单分组。

        return orderService.list(userId, status, current, size);
    }

    @ResponseBody
    @ApiOperation("获取订单详情")
    @PostMapping("/detail")
    public Result<OrderDetailDto> detail(@ApiParam("订单id") Long orderId) {
        return orderService.detail(orderId);
    }

    /**
     * TODO Func()
     *  -   OmsPortalOrderController.paySuccess()
     、     */
}