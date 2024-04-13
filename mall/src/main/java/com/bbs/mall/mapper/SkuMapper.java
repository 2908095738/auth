package com.bbs.mall.mapper;

import com.bbs.mall.bo.StockBO;
import com.bbs.mall.entity.OrderItem;
import com.bbs.mall.entity.Sku;
import com.github.yulichang.base.MPJBaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SkuMapper extends MPJBaseMapper<Sku> {

    /**
     * 取消订单时，商品库存回收
     *
     * @param items
     * @return
     */
    int releaseStock(@Param("items") List<OrderItem> items);

    /**
     * 变更库存
     *
     * @param stocks 库存列表
     * @return
     */
    int updateStock(@Param("stocks") List<StockBO> stocks);
}