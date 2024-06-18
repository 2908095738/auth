package com.bbs.mall.service.impl;

import com.bbs.mall.entity.OrderItem;
import com.bbs.mall.mapper.OrderItemMapper;
import com.bbs.mall.service.OrderItemService;
import com.github.yulichang.base.MPJBaseServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class OrderItemServiceImpl extends MPJBaseServiceImpl<OrderItemMapper, OrderItem> implements OrderItemService {
}