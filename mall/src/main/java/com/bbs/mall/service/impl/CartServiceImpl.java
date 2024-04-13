package com.bbs.mall.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.mall.bo.LowProductBO;
import com.bbs.mall.converter.CartConverter;
import com.bbs.mall.dto.CartDto;
import com.bbs.mall.dto.CartLowDto;
import com.bbs.mall.entity.Cart;
import com.bbs.mall.enums.RedisKeys;
import com.bbs.mall.mapper.CartMapper;
import com.bbs.mall.service.CartService;
import com.bbs.mall.service.ProductService;
import com.bbs.mall.test.Constant;
import com.bbs.mall.util.RedisUtil;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl extends MPJBaseServiceImpl<CartMapper, Cart> implements CartService {
    @Resource
    private RedisUtil redis;
    @Autowired
    private ProductService productService;

    @Autowired
    private CartConverter cartConverter;

    @Override
    public Result create(Cart cart) {
        String key = RedisKeys.NEW_CART_USER.key() + cart.getUserId();
        String hashKey = RedisKeys.NEW_CART_PRODUCT.key() + cart.getProductId();

        boolean isDone = false;
        String json = (String) redis.hashGet(key, hashKey);
        if (Objects.nonNull(json)) {
            if (!json.equals("-1")) {//缓存删除，但key可能删除不及时，所以二次确认
                isDone = save(cart);
            }
        }

        if (isDone) {//从DB获取id，放入缓存中，不然后续接口的请求参数过于繁琐。
            Long cartId = new MPJLambdaWrapper<Long>()
                    .select(Cart::getId)
                    .eq(Cart::getUserId, cart.getUserId())
                    .eq(Cart::getProductId, cart.getProductId())
                    .one(Long.class);

            /**
             * TODO 隐患：
             *  -   例如用户把商品加到购物车后立马下订单支付，且这时购物车只有这一个商品。
             *  -   就会导致商品存入缓存，但因为延时队列原因，导致还是不能获取购物车商品id，进而还是会导致后续接口的请求参数复杂。
             */

            JSONObject jObj = JSON.parseObject(json);
            jObj.put("id", cartId);
            redis.hashSet(key, hashKey, jObj.toString());
            return Result.success();
        } else {
            return Result.failed("db insert err");
        }
    }

    @Override
    public List<CartDto> list(Long userId, Integer current, Integer size) {
        return new MPJLambdaWrapper<CartDto>()
                .select(Cart::getId, Cart::getProductBrand, Cart::getQuantity)
                .select(Cart::getProdSN, Cart::getProdSkuCode, Cart::getProdSkuJson)
                .select(Cart::getPrice, Cart::getProductName, Cart::getProductImg)
                .select(Cart::getProductId, Cart::getSkuId, Cart::getCategoryId)
                .select(Cart::getSort, Cart::getCreateTime, Cart::getUpdateTime)
                .select(Cart::getBrandId)

                .gt(Cart::getSort, -1)
                .eq(Cart::getDeleteStatus, 1)
                .eq(Cart::getUserId, userId)

                .orderByAsc(Cart::getSort)
                .page(new Page(current, size))
                .getRecords();
    }

    @Override
    public List<CartDto> list(Long userId, List<Long> prods) {
        return new MPJLambdaWrapper<CartDto>()
                .select(Cart::getId, Cart::getProductBrand, Cart::getQuantity)
                .select(Cart::getProdSN, Cart::getProdSkuCode, Cart::getProdSkuJson)
                .select(Cart::getPrice, Cart::getProductName, Cart::getProductImg)
                .select(Cart::getProductId, Cart::getSkuId, Cart::getCategoryId)
                .select(Cart::getSort, Cart::getCreateTime, Cart::getUpdateTime)
                .select(Cart::getBrandId)

                .eq(Cart::getDeleteStatus, 1)
                .eq(Cart::getUserId, userId)
                .in(Cart::getProductId, prods)
                .list();
    }

    @Override
    public List<CartDto> list(List<Long> cartIds) {
        return new MPJLambdaWrapper<CartDto>()
                .select(Cart::getId, Cart::getProductBrand, Cart::getQuantity)
                .select(Cart::getProdSN, Cart::getProdSkuCode, Cart::getProdSkuJson)
                .select(Cart::getPrice, Cart::getProductName, Cart::getProductImg)
                .select(Cart::getProductId, Cart::getSkuId, Cart::getCategoryId)
                .select(Cart::getSort, Cart::getCreateTime, Cart::getUpdateTime)
                .select(Cart::getBrandId)

                .eq(Cart::getDeleteStatus, 1)
                .in(Cart::getId, cartIds)
                .list();
    }

    @Override
    public List<CartLowDto> listLow(List<CartDto> dtoList) {
        Map<Long, List<CartDto>> spuMap = groupBySpu(dtoList);

        //查询所有商品的优惠信息
        List<Long> prodIds = dtoList.stream().map(d -> d.getProdId()).collect(Collectors.toList());
        List<LowProductBO> lowMsgList = productService.getLowList(prodIds);

        List<CartLowDto> result = new ArrayList();
        for (Map.Entry<Long, List<CartDto>> entry : spuMap.entrySet()) {
            //获取当前促销商品信息
            Long prodId = entry.getKey();
            LowProductBO low = lowMsgList.stream().filter(l -> l.getId().equals(prodId)).findFirst().get();

            switch (low.getLowType()) {
                case 0://没有促销使用原价
                    noLow(result, entry.getValue(), low);
                    break;
                case 1://单品促销
                    oneLow(result, entry.getValue(), low);
                    break;
                case 2://待定
                    break;
                case 3://打折优惠
                    discount(result, entry.getValue(), low);
                    break;
                case 4://使用满减价格
                    reduce(result, entry.getValue(), lowMsgList, low);
                    break;
                case 5://限时购
                    break;
            }
        }
        return result;
    }


    /**
     * 以spu为单位对购物车中商品进行分组
     *
     * @param dtoList
     * @return
     */
    private Map<Long, List<CartDto>> groupBySpu(List<CartDto> dtoList) {
        Map<Long, List<CartDto>> result = new TreeMap<>();
        for (CartDto dto : dtoList) {
            List<CartDto> tmpList = result.get(dto.getProdId());
            if (tmpList.isEmpty()) {
                tmpList = new ArrayList();
                tmpList.add(dto);
                result.put(dto.getProdId(), tmpList);
            } else {
                tmpList.add(dto);
            }
        }
        return result;
    }

    /**
     * 没有促销使用原价
     *
     * @param result
     * @param dtoList 购物车商品列表
     * @param low     当前商品促销信息
     */
    private void noLow(List<CartLowDto> result, List<CartDto> dtoList, LowProductBO low) {
        dtoList.forEach(d -> {
            //购物车促销商品id、价格、信息、差价赋值
            CartLowDto tmp = new CartLowDto();
            tmp.setProductId(d.getProdId());
            tmp.setNowPrice(d.getPrice());
            tmp.setLowMsg("无优惠");
            tmp.setGap(new BigDecimal(0));

            //购物车促销商品库存赋值
            LowProductBO.LowSkuBO sku = low.getSkus().stream().filter(s -> s.getSkuId().equals(d.getSkuId())).findFirst().get();
            if (Objects.nonNull(sku)) {
                Integer realStock = sku.getStock() - sku.getLockStock();
                tmp.setRealStock(realStock);
            }

            result.add(tmp);
        });
    }

    /**
     * 单品促销
     *
     * @param result
     * @param dtoList 购物车商品列表
     * @param low     当前商品促销信息
     */
    private void oneLow(List<CartLowDto> result, List<CartDto> dtoList, LowProductBO low) {
        dtoList.forEach(d -> {
            //购物车促销商品id、信息赋值
            CartLowDto tmp = new CartLowDto();
            tmp.setProductId(d.getProdId());
            tmp.setLowMsg("单品促销");

            //获取促销商品SKU原价
            LowProductBO.LowSkuBO sku = low.getSkus().stream().filter(s -> s.getSkuId().equals(d.getSkuId())).findFirst().get();
            BigDecimal skuPrice = sku.getPrice();

            //购物车促销商品价格赋值
            tmp.setNowPrice(skuPrice);

            //购物车促销商品差价赋值
            BigDecimal gap = skuPrice.subtract(sku.getLowPrice());
            tmp.setGap(gap);

            //购物车促销商品库存赋值
            Integer realStock = sku.getStock() - sku.getLockStock();
            tmp.setRealStock(realStock);

            result.add(tmp);
        });
    }

    /**
     * 打折优惠
     *
     * @param result
     * @param dtoList 购物车商品列表
     * @param low     当前商品促销信息
     */
    private void discount(List<CartLowDto> result, List<CartDto> dtoList, LowProductBO low) {
        //获取同类商品数量总和
        Integer count = dtoList.stream().map(d -> d.getQuantity()).reduce(0, Integer::sum);

        //同类商品按数量从高到低排列
        low.getLadders().sort((l, r) -> r.getCount() - l.getCount());

        //获取满足所需数量的同类商品
        LowProductBO.LowLadderBO ladderBO = low.getLadders().stream().filter(l -> count >= l.getCount()).findFirst().orElse(null);

        if (Objects.nonNull(ladderBO)) {
            dtoList.forEach(d -> {
                //购物车促销商品id、价格、信息赋值
                CartLowDto tmp = new CartLowDto();
                tmp.setProductId(d.getProdId());
                tmp.setNowPrice(d.getPrice());
                tmp.setLowMsg(getLowMsg(ladderBO, 1));

                //获取促销商品SKU原价
                LowProductBO.LowSkuBO sku = low.getSkus().stream().filter(s -> s.getSkuId().equals(d.getSkuId())).findFirst().get();
                BigDecimal oriPrice = sku.getPrice();

                //购物车促销商品差价赋值
                BigDecimal tmpPrice = ladderBO.getDiscount().multiply(oriPrice);
                BigDecimal gap = oriPrice.subtract(tmpPrice);
                tmp.setGap(gap);

                //购物车促销商品库存赋值
                Integer realStock = sku.getStock() - sku.getLockStock();
                tmp.setRealStock(realStock);

                result.add(tmp);
            });
        } else {
            noLow(result, dtoList, low);
        }
    }

    /**
     * 获取促销商品信息
     *
     * @param <T>  LowProductBO.LowLadderBO/LowProductBO.LowReduceBO
     * @param type 1:折扣信息   2.满减信息
     * @return
     */
    private <T> String getLowMsg(T bo, int type) {
        String result = "";
        switch (type) {
            case 1://折扣信息
                LowProductBO.LowLadderBO ladderBO = (LowProductBO.LowLadderBO) bo;
                String discountFormat = "打折优惠：满%d件，打%.1f折";
                result = String.format(discountFormat,
                        ladderBO.getCount(),
                        ladderBO.getDiscount().multiply(new BigDecimal(10)).doubleValue());
            case 2://满减信息
                LowProductBO.LowReduceBO reduceBO = (LowProductBO.LowReduceBO) bo;
                String reduceFormat = "满减优惠：满%.2f元，减%.2f元";
                result = String.format(reduceFormat,
                        reduceBO.getFullPrice().doubleValue(),
                        reduceBO.getReducePrice().doubleValue());
        }
        return result;
    }

    /**
     * 满减优惠
     *
     * @param result
     * @param dtoList    购物车商品列表
     * @param lowMsgList 查询所有商品的优惠信息
     * @param nowLow     当前商品促销信息
     */
    private void reduce(List<CartLowDto> result, List<CartDto> dtoList, List<LowProductBO> lowMsgList, LowProductBO nowLow) {
        //购物车商品促销总价
        BigDecimal amount = dtoList.stream().map(d -> {
            //获取当前促销商品信息
            //TODO bo的获取可能稍显冗余?
            LowProductBO bo = lowMsgList.stream().filter(b -> b.getId().equals(d.getProdId())).findFirst().get();

            //获取促销商品SKU原价
            LowProductBO.LowSkuBO sku = bo.getSkus().stream().filter(s -> s.getSkuId().equals(d.getSkuId())).findFirst().get();

            //当前促销商品总价
            return sku.getPrice().multiply(new BigDecimal(d.getQuantity()));
        }).reduce(new BigDecimal(0), BigDecimal::add);

        //按满足价格条件从高到低排列
        nowLow.getReduces().sort((l, r) -> r.getFullPrice().subtract(l.getFullPrice()).intValue());

        //获取当前商品适合的满减规则
        LowProductBO.LowReduceBO reduce = nowLow.getReduces().stream().filter(r -> amount.subtract(r.getFullPrice()).intValue() >= 0).findFirst().orElse(null);

        if (Objects.nonNull(reduce)) {
            dtoList.forEach(d -> {
                //购物车促销商品id、价格、信息赋值
                CartLowDto tmp = new CartLowDto();
                tmp.setProductId(d.getProdId());
                tmp.setNowPrice(d.getPrice());
                tmp.setLowMsg(getLowMsg(reduce, 2));

                //获取促销商品SKU原价
                LowProductBO.LowSkuBO sku = nowLow.getSkus().stream().filter(s -> s.getSkuId().equals(d.getSkuId())).findFirst().get();
                BigDecimal oriPrice = sku.getPrice();

                //获取促销商品差价
                BigDecimal gap = oriPrice.divide(amount, RoundingMode.HALF_EVEN).multiply(reduce.getReducePrice());
                tmp.setGap(gap);

                //获取促销商品库存
                Integer realStock = sku.getStock() - sku.getLockStock();
                tmp.setRealStock(realStock);

                result.add(tmp);
            });
        } else {
            noLow(result, dtoList, nowLow);
        }
    }

    @Override
    public Result updateQuantity(Cart cart) {
        String key = RedisKeys.NEW_CART_USER.key() + cart.getUserId();
        String hashKey = RedisKeys.NEW_CART_PRODUCT.key() + cart.getProductId();

        boolean isDone = false;

        String json = (String) redis.hashGet(key, hashKey);
        if (Objects.nonNull(json) && !json.equals("-1")) {//二次确认规避缓存删除
            CartDto dtoTmp = JSON.parseObject(json, CartDto.class);
            if (dtoTmp.getIsMQUpd()) {//修改缓存购物车商品数量
                Cart toDB = cartConverter.toEntity(dtoTmp);
                toDB.setUserId(cart.getUserId());
                isDone = update()
                        .eq("user_id", toDB.getUserId())
                        .eq("product_id", toDB.getProductId())
                        .update(toDB);

                //修改缓存购物车商品状态，防止未来不再更新DB。
                dtoTmp.setIsMQUpd(false);
                redis.hashSet(key, hashKey, JSON.toJSONString(dtoTmp));
            }
        } else if (Objects.nonNull(cart.getQuantity())) {//修改DB购物车商品数量
            Long cartId = new MPJLambdaWrapper<Cart>()//规避DB删除
                    .select(Cart::getId)
                    .eq(Cart::getUserId, cart.getUserId())
                    .eq(Cart::getProductId, cart.getProductId())
                    .one(Long.class);

            if (Objects.nonNull(cartId)) {
                cart.setId(cartId);
                isDone = updateById(cart);
            }
        }

        if (isDone) {
            return Result.success();
        } else {
            return Result.failed("db upd fail");
        }
    }

    @Override
    public Result updateAttr(Long userId, Long prodId) {
        String key = RedisKeys.NEW_CART_USER.key() + userId;
        String hashKey = RedisKeys.NEW_CART_PRODUCT.key() + prodId;
        String json = (String) redis.hashGet(key, hashKey);

        if (!json.isEmpty()) {
            if (!json.equals("-1")) {//缓存删除可能不会及时，所以先把缓存数据设为-1作为双重保险机制
                CartDto dto = JSON.parseObject(json, CartDto.class);

                Long createTime = dto.getCreateTime().getTime();
                Long updateTime = dto.getUpdateTime().getTime();

                boolean isDone = false;
                Cart toDB = cartConverter.toEntity(dto);
                if ((updateTime - createTime) < Constant.CART_DEL_TIME) {//商品添加到购物车后，短时间内改规格则直接改DB
                    isDone = updateById(toDB);
                } else {//商品添加到购物车后，长时间后改规格则逻辑删除原规格，再添加新规格
                    Cart tmp = new MPJLambdaWrapper<Cart>()//规避修改后用户秒删
                            .selectAll(Cart.class)
                            .eq(Cart::getDeleteStatus, 1)
                            .eq(Cart::getProductId, prodId)
                            .eq(Cart::getUserId, userId)
                            .one();

                    if (Objects.nonNull(tmp)) {
                        isDone = update()//逻辑删除原规格
                                .set("delete_status", 2)
                                .set("update_time", toDB.getUpdateTime())
                                .eq("id", tmp.getId())
                                .update();

                        if (isDone) {//添加新规格
                            tmp.setId(0L);
                            tmp.setPrice(toDB.getPrice());
                            tmp.setProductImg(toDB.getProductImg());
                            tmp.setSkuId(toDB.getSkuId());
                            tmp.setCreateTime(toDB.getUpdateTime());
                            tmp.setUpdateTime(toDB.getUpdateTime());
                            return create(tmp);
                        } else {
                            return Result.failed("upd fail");
                        }

                    } else {
                        return Result.failed("cart's product del");
                    }
                }

                if (isDone) {
                    return Result.success();
                } else {
                    return Result.failed("upd fail");
                }
            } else {
                //TDDO 打日志(缓存实际并没有删)
                return Result.failed("cache del later");
            }
        } else {
            //TDDO 打日志(缓存实际并没有删)
            return Result.failed("cache del later");
        }
    }

    @Override
    public boolean delete(Cart cart) {
        boolean isDone = false;
        if (Objects.nonNull(cart.getProductId())) {//从缓存中删除
            isDone = update()
                    .eq("user_id", cart.getUserId())
                    .eq("product_id", cart.getProductId())
                    .update(cart);
        }

        if (Objects.nonNull(cart.getId())) {//直接从DB逻辑删除
            isDone = updateById(cart);
        }

        return isDone;
    }
}