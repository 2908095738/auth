package com.bbs.mall.cache;

import com.bbs.Result;
import com.bbs.mall.dto.CartDto;
import com.bbs.mall.dto.param.UpdCartParam;
import com.bbs.mall.entity.Cart;
import io.swagger.annotations.ApiOperation;

import java.util.List;

public interface CartCache {
    void create(CartDto dto, Long userId);

    @ApiOperation("从缓存中获取购物车商品列表")
    List<CartDto> list(Long userId);

    /**
     * 修改指定购物车商品的数量
     *
     * @param userId   用户id
     * @param prodId   商品id
     * @param quantity 数量
     * @return
     */
    Result updateQuantity(Long userId, Long prodId, Integer quantity);

    /**
     * 修改指定购物车商品的规格
     *
     * @param param  更改请求实体
     * @param userId 用户id
     * @return
     */
    Result updateAttr(UpdCartParam param, Long userId);

    /**
     * 删除购物车商品
     *
     * @param userId
     * @param mergeId prodId/cartId；prodId：从缓存查询的数据；cartId：从DB查询的数据
     * @return
     */
    Result delete(Long userId, Long mergeId);
}