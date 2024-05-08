package com.bbs.mall.enums;

import cn.hutool.core.util.EnumUtil;
import lombok.Getter;

import java.util.Map;

@Getter
public enum RedisKeys {

    NEW_CART_USER("new_cart_user:", "购物车用户id"),
    NEW_CART_PRODUCT("new_cart_product:", "购物车商品id"),

    NEW_CART_DB_SORT("new_cart_db_sort:", "DB购物车商品顺序"),

    /**
     * 因为购物车列表是分页设计，第二页开始是从DB获取数据。
     * 因为用户改购物车商品的参数，可能会多次修改，如果每次改都直接操作DB很低效，所以把头几个从DB查询的数据放入缓存，再用redssion对键监听。
     */
    UPD_CART_TMP("upd_cart_tmp:", "临时更新购物车商品");
    private final String prefix;

    private final String description;

    private static final String LOCK_SUFFIX = "lock";

    public static final Map<String, RedisKeys> map = EnumUtil.getEnumMap(RedisKeys.class);

    RedisKeys(String prefix, String description) {
        this.prefix = prefix;
        this.description = description;
    }

    public String key() {
        return prefix;
    }

    /**
     * key
     *
     * @param mark 业务标识 / 表名 / 主键
     * @return key
     */
    public String key(String mark) {
        return prefix + ":" + mark;
    }

    @Override
    public String toString() {
        return key();
    }
}