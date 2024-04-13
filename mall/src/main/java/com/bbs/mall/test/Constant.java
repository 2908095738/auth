package com.bbs.mall.test;

//TODO 暂时的常量，后面这里的常量估计都要改用zookeeper实现[动态]配置
public final class Constant {
    //购物车缓存商品上限；购物车第一部分商品从缓存取，其余从DB取，所以采取类似分页的方式来取购物车商品，所以取第一个分页时，取的数目就是这里的上限。
    public static final int CART_SIZE = 12;//TODO 缓存商品上限要和前端分页的条数一致

    public static final int CART_DEL_TIME = 1000 * 2;
}
