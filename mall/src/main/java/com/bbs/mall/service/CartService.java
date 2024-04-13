package com.bbs.mall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bbs.Result;
import com.bbs.mall.dto.CartDto;
import com.bbs.mall.dto.CartLowDto;
import com.bbs.mall.entity.Cart;

import java.util.List;

public interface CartService extends IService<Cart> {

    Result create(Cart cart);

    /**
     * 分页获取购物车商品
     *
     * @param current 页码
     * @param size    条数
     * @return
     */
    List<CartDto> list(Long userId, Integer current, Integer size);

    /**
     * 获取购物车商品列表
     *
     * @param userId 用户id
     * @param prods  商品id列表
     * @return
     */
    List<CartDto> list(Long userId, List<Long> prods);

    /**
     * 获取购物车商品列表
     *
     * @param cartIds 购物车商品id列表
     * @return
     */
    List<CartDto> list(List<Long> cartIds);

    /**
     * 获取购物车中所有商品的促销信息
     *
     * @param dtoList 购物车商品列表
     * @return
     */
    List<CartLowDto> listLow(List<CartDto> dtoList);

    Result updateQuantity(Cart cart);

    /**
     * 修改指定购物车商品的规格(子页面不提供改数量)
     *
     * @param userId 用户id
     * @param prodId 商品id
     * @return
     */
    Result updateAttr(Long userId, Long prodId);

    /**
     * 逻辑删除购物车商品
     *
     * @param cart
     * @return
     */
    boolean delete(Cart cart);
}