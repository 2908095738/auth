package com.bbs.mall.service.impl;

import com.bbs.mall.entity.Sku;
import com.bbs.mall.mapper.SkuMapper;
import com.bbs.mall.service.SkuService;
import com.github.yulichang.base.MPJBaseServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class SkuServiceImpl extends MPJBaseServiceImpl<SkuMapper, Sku> implements SkuService {
}