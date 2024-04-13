package com.bbs.mall.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.mall.bo.ItemTipBO;
import com.bbs.mall.bo.OrderLiteBO;
import com.bbs.mall.bo.OrderSendBO;
import com.bbs.mall.bo.StockBO;
import com.bbs.mall.converter.CartConverter;
import com.bbs.mall.converter.OrderConverter;
import com.bbs.mall.dto.*;
import com.bbs.mall.dto.param.OrderParam;
import com.bbs.mall.entity.*;
import com.bbs.mall.enums.DBType;
import com.bbs.mall.enums.RedisKeys;
import com.bbs.mall.mapper.OrderMapper;
import com.bbs.mall.mapper.SkuMapper;
import com.bbs.mall.mq.RabbitmqConfig;
import com.bbs.mall.mq.RabbitmqSend;
import com.bbs.mall.service.CartService;
import com.bbs.mall.service.OrderItemService;
import com.bbs.mall.service.OrderService;
import com.bbs.mall.service.SkuService;
import com.bbs.mall.util.RedisUtil;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class OrderServiceImpl extends MPJBaseServiceImpl<OrderMapper, Order> implements OrderService {

    @Resource
    private RedisUtil redis;

    @Autowired
    private CartConverter cartConverter;

    @Autowired
    private CartService cartService;

    @Autowired
    private SkuService skuService;

    @Autowired
    private OrderItemService orderItemService;

    @Resource
    private RabbitmqSend send;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private OrderConverter orderConverter;

    @Override
    public OrderConfirmDto getConfirmOrder(Long userId, List<Long> prodIds) {
        OrderConfirmDto result = new OrderConfirmDto();
        String key = RedisKeys.NEW_CART_USER.key() + userId;
        String hashKeyL = RedisKeys.NEW_CART_PRODUCT.key();

        List<CartDto> getLowTmps = new ArrayList();//获取订单商品、订单促销商品列表
        List<Long> skuInTmps = new ArrayList();//获取商品Sku属性列表
        List<Long> cartInTmps = new ArrayList();//获取购物车商品列表
        Set<String> cacheIds = redis.colGet(key);
        for (Long pordId : prodIds) {
            if (cacheIds.contains(pordId.toString())) {//缓存取购物车商品
                String json = (String) redis.hashGet(key, hashKeyL + pordId);
                if (!json.equals("-1")) {
                    CartDto dto = JSON.parseObject(json, CartDto.class);
                    getLowTmps.add(dto);
                    skuInTmps.add(pordId);
                }
            } else {//DB取购物车商品(只获取订单id，后面通过一条SQL查询列表数据)
                cartInTmps.add(pordId);
                skuInTmps.add(pordId);
            }
        }

        //从DB获取的购物车商品列表，与缓存获取的购物车商品列表合并
        if (!cartInTmps.isEmpty()) {
            List<CartDto> tmp = cartService.list(userId, cartInTmps);
            getLowTmps.addAll(tmp);
        }

        //获取订单商品列表缺失实例域
        MPJLambdaWrapper<Sku> warp = new MPJLambdaWrapper(Sku.class);
        Map<Long, Sku> getJsonTmps = warp.select(Sku::getProductId, Sku::getProdJson)
                .in(Sku::getProductId, skuInTmps)
                .list()
                .stream()
                .collect(Collectors.toMap(Sku::getProductId, s -> s));

        //订单商品列表赋值
        List<OrderCartDto> ocDtos = getLowTmps.stream().map(cartConverter::toDto)
                .map(oc -> {//补齐订单商品列表缺失实例域
                    Long pordId = oc.getProductId();
                    String json = getJsonTmps.get(pordId).getProdJson();
                    oc.setProdJson(json);
                    return oc;
                }).collect(Collectors.toList());
        result.setOcDtos(ocDtos);

        //订单促销商品列表赋值
        List<CartLowDto> clDto = cartService.listLow(getLowTmps);
        result.setClDtos(clDto);

        //收货地址列表赋值
        MPJLambdaWrapper<AddressDto> addrWrap = new MPJLambdaWrapper(Address.class);
        List<AddressDto> addrDtos = addrWrap
                .select(Address::getId, Address::getName, Address::getPhone)
                .select(Address::getPostCode, Address::getProvince, Address::getCity)
                .select(Address::getRegion, Address::getDetail, Address::getOneStatus)

                .eq(Address::getUserId, userId)
                .list();
        result.setAddresses(addrDtos);

        //订单商品金额赋值
        OrderConfirmDto.CalcAmount amount = new OrderConfirmDto.CalcAmount();
        amount.setSendAmount(new BigDecimal(0));//TODO 运费无实现？应该在订单确认页就能看到运费。

        Map<Long, CartDto> amountTmps = getLowTmps.stream()
                .collect(Collectors.toMap(CartDto::getProdId, c -> c));

        //计算子金额
        BigDecimal totalAmount = new BigDecimal(0);
        BigDecimal lowAmount = new BigDecimal(0);
        for (CartLowDto dto : clDto) {
            CartDto tmp = amountTmps.get(dto.getProductId());
            BigDecimal price = tmp.getPrice();
            Integer quantity = tmp.getQuantity();

            BigDecimal tmpTotal = price.multiply(new BigDecimal(quantity));
            BigDecimal tmpLow = dto.getGap().multiply(new BigDecimal(quantity));

            totalAmount = totalAmount.add(tmpTotal);
            lowAmount = lowAmount.add(tmpLow);
        }

        //订单商品金额赋值
        amount.setTotalAmount(totalAmount);
        amount.setLowAmount(lowAmount);
        amount.setPayAmount(totalAmount.subtract(lowAmount));//TODO 运费未参与运算，如前所言
        result.setCalcAmount(amount);

        return result;
    }

    @Override
    public Result createOrder(OrderParam param, Long userId) {
        List<OrderItem> items = new ArrayList();

        if (Objects.isNull(param.getAddrId())) {
            return Result.failed("no have address");
        }

        //获取购物车商品
        List<CartDto> cartDtos = new ArrayList();
        boolean isProdIds = Objects.nonNull(param.getProdIds());
        if (isProdIds) {
            List<CartDto> tmp = cartService.list(userId, param.getProdIds());
            cartDtos.addAll(tmp);
        }

        //获取购物车商品促销信息
        boolean isCartIds = Objects.nonNull(param.getCartIds());
        if (isCartIds) {
            List<CartDto> tmp = cartService.list(param.getCartIds());
            cartDtos.addAll(tmp);
        }

        //获取商品促销信息
        List<CartLowDto> lowTmp = cartService.listLow(cartDtos);
        Map<Long, CartLowDto> lowMap = lowTmp.stream().collect(Collectors.toMap(CartLowDto::getProductId, l -> l));

        //订单商品赋值
        cartDtos.forEach(d -> {
            CartLowDto lowDto = lowMap.get(d.getProdId());
            initItem(items, d, lowDto.getLowMsg(), lowDto.getGap());
        });

        //订单有商品无库存
        Map<String, Object> stockMap = hasStock(cartDtos, lowMap);
        if (!(boolean) stockMap.get("isStock")) {
            String noStockTip = String.format("该商品无库存：%s", stockMap.get("prodName"));
            return Result.failed(noStockTip);
        }

        initPayPrice(items);//订单商品-实际支付金额赋值

        lockStock(cartDtos);//锁定订单商品库存

        //TODO 前置订单所需额外计算值，因为后面订单会入库多份高强度重复
        BigDecimal totalAmount = calcTotalAmount(items);
        BigDecimal lowAmount = calcLowAmount(items, lowMap);
        String lowMsgJson = initMsgJson(lowTmp);

        //获取地址
        Address address = new MPJLambdaWrapper<Address>(Address.class)
                .selectAll(Address.class)
                .eq(Address::getId, param.getAddrId())
                .one();

        //获取订单列表
        Date now = new Date();
        List<Order> orders = new ArrayList();
        List<String> orderSNs = new ArrayList();
        items.stream()
                .collect(Collectors.groupingBy(OrderItem::getBrandId))
                .entrySet()
                .forEach(e -> {
                    Order tmp = initOrder(totalAmount, lowAmount, lowMsgJson, userId, param.getPayType(), address, e.getKey(), now);
                    String orderSN = tmp.getOrderSN();
                    e.getValue().forEach(i -> i.setOrderSN(orderSN));//同品牌订单SN赋值

                    orderSNs.add(orderSN);
                    orders.add(tmp);
                });

        boolean isDone = saveBatch(orders);
        if (!isDone) {
            return Result.failed("db insert fail");
        }

        isDone = orderItemService.saveBatch(items);
        if (!isDone) {
            return Result.failed("db insert fail");
        }

        //下单的商品从购物车中清除
        isDone = clearCartByOrder(lowMap.keySet(), userId);
        if (!isDone) {
            return Result.failed("del cart fail");
        }

        //定时取消未支付订单
        //TODO 目前采用订单SN码确定唯一性
        send.send(RabbitmqConfig.EXCHANGE_DIRECT_MALL, RabbitmqConfig.QUEUE_ORDER_CANCEL, JSON.toJSONString(orderSNs));

        return Result.success();
    }

    /**
     * 初始化订单商品并添加到列表
     *
     * @param result 订单商品列表
     * @param dto    购物车商品
     * @param lowMsg 促销信息
     * @param lowGap 减免价格
     */
    private void initItem(List<OrderItem> result, CartDto dto, String lowMsg, BigDecimal lowGap) {
        OrderItem tmp = new OrderItem();
        tmp.setProdBrand(dto.getProdBrand());
        tmp.setProdSN(dto.getProdSN());
        tmp.setProdSkuCode(dto.getProdSkuCode());
        tmp.setProdPic(dto.getProdPic());
        tmp.setProdName(dto.getProdName());
        tmp.setProdSkuJson(dto.getProdSkuJson());
        tmp.setProdQuantity(dto.getQuantity());
        tmp.setProdPrice(dto.getPrice());
        tmp.setBrandId(dto.getBrandId());

        tmp.setProdId(dto.getProdId());
        tmp.setProdSkuId(dto.getSkuId());
        tmp.setProdCateId(dto.getProdCateId());

        tmp.setLowMsg(lowMsg);
        tmp.setLowGap(lowGap);

        result.add(tmp);
    }

    /**
     * 初始化订单实例并添加到列表
     *
     * @param totalAmount 订单总金额
     * @param lowAmount   促销后金额
     * @param lowMsgJson  促销信息json
     * @param userId      用户id
     * @param payType     支付方式
     * @param address     地址
     * @param brandId     品牌id
     * @param now         当前时间
     * @return
     */
    private Order initOrder(BigDecimal totalAmount, BigDecimal lowAmount, String lowMsgJson, Long userId, Integer payType, Address address, Long brandId, Date now) {
        //订单赋值
        Order order = new Order();
        order.setTotalAmount(totalAmount);
        order.setSendAmount(new BigDecimal(0));//TOOD 运费暂且设置0
        order.setLowAmount(lowAmount);
        order.setLowMsgJson(lowMsgJson);
        order.setPayAmount(calcPayAmount(order));
        order.setUserId(userId);
        order.setCreateTime(now);
        order.setPayType(payType);
        order.setStatus(0);

        //订单-地址相关实例域赋值
        order.setShouName(address.getName());
        order.setShouPhone(address.getPhone());
        order.setShouPostCode(address.getPostCode());
        order.setShouProvince(address.getProvince());
        order.setShouCity(address.getCity());
        order.setShouRegion(address.getRegion());
        order.setShouDetail(address.getDetail());

        //订单-状态实例域赋值
        order.setShouStatus(0);
        order.setDelStatus(0);

        order.setOrderSN(brandId + "_orderSN no impl");//TODO 实现参考：OmsPortalOrderServiceImpl.generateOrderSn()
        order.setAutoShouDay(3);//TODO 参考实现专门用[oms_order_setting]表查询设值

        return order;
    }

    @Override
    public Result cancelOrder(List<String> orderSNs) {
        //查询对应未支付、未删除订单
        Order order = new MPJLambdaWrapper<Order>()
                .selectAll(Order.class)
                .eq(Order::getStatus, 0)
                .eq(Order::getDelStatus, 0)
                .in(Order::getOrderSN, orderSNs)
                .one();

        //已支付直接返回
        if (Objects.isNull(order)) {
            return Result.success();
        }

        //变更订单状态
        boolean isDone = update()
                .set("status", 4)
                .update(order);
        if (!isDone) {
            return Result.failed("upd order fail");
        }

        //获取订单商品
        List<OrderItem> items = new MPJLambdaWrapper<OrderItem>(OrderItem.class)
                .selectAll(OrderItem.class)
                .in(OrderItem::getOrderSN, orderSNs)
                .list();
        if (Objects.isNull(items) || items.isEmpty()) {
            return Result.failed("order err");
        }

        //商品库存回收
        //TODO 把表数据所有查询，但实际使用却只有两个字段
        int line = skuMapper.releaseStock(items);
        if (line == 0) {
            return Result.failed("update stock fail");
        }

        return Result.success();
    }

    //TODO 定时器调用
    @Override
    public Result cancel2LongOrder() {
        //查询超时、未支付订单
        Integer minute = 5;//TODO 订单超时标识符暂且用这种方式
        String timeFormat = "date_add(NOW(), INTERVAL -%d MINUTE)";
        List<OrderItem> items = new MPJLambdaWrapper<OrderItem>()
                .selectAll(OrderItem.class)
                .leftJoin(OrderItem.class, OrderItem::getOrderId, Order::getId)
                .eq(Order::getStatus, 0)
                .ltSql(Order::getCreateTime, String.format(timeFormat, minute))
                .list();

        if (Objects.isNull(items)) {
            return Result.success();
        }

        //获取订单id列表
        List<Long> orderIds = items.stream().map(OrderItem::getOrderId).collect(Collectors.toList());

        //变更订单状态
        boolean isDone = lambdaUpdate()
                .set(Order::getStatus, 4)
                .in(Order::getId, orderIds)
                .update();
        if (!isDone) {
            return Result.failed("upd order fail");
        }

        //变更商品SKU库存
        int line = skuMapper.releaseStock(items);
        if (line == 0) {
            return Result.failed("upd sku fail");
        }

        return Result.success();
    }

    @Override
    public Result paySuccess(String orderSN, Integer payType) {
        //变更订单状态
        boolean isDone = lambdaUpdate()
                .set(Order::getStatus, 1)
                .set(Order::getPayTime, new Date())
                .set(Order::getPayType, payType)
                .eq(Order::getOrderSN, orderSN)
                .update();

        if (!isDone) {
            return Result.failed("upd order fail");
        }

        //获取变更库存所用数据
        MPJLambdaWrapper<StockBO> wrap = new MPJLambdaWrapper(OrderItem.class);
        List<StockBO> stockBOS = wrap
                .select(OrderItem::getProdSkuId, OrderItem::getProdQuantity)
                .eq(OrderItem::getOrderSN, orderSN)
                .list();

        //变更库存
        int line = skuMapper.updateStock(stockBOS);
        if (line == 0) {
            return Result.failed("update stock fail");
        }

        return Result.success();
    }

    //TODO 目前实现思路：同一订单且同一品牌才是一个子订单，如果同一订单有多个品牌则意味多个子订单，导致SN码重复。
    @Override
    public Result<Page> list(Long userId, Integer status, Integer current, Integer size) {
        switch (status) {
            case 0://待付款
            case 1://待发货
                return toSendList(userId, status, current, size);
            case 2://已发货
            case 3://已完成
                return sendList(userId, status, current, size);
            case 4://已关闭
                //TODO
                break;
        }

        if (status == -1) {
            status = null;
        }
        //TODO
        return null;
    }

    //TODO 分页暂返回错误，后续用DB的分组实现
    private Result toSendList(Long userId, Integer status, Integer current, Integer size) {
        //获取订单简要信息分页
        DynamicDataSourceContextHolder.push(DBType.MALLC.getDbName());
        Page<OrderLiteBO> page = new MPJLambdaWrapper<OrderLiteBO>()
                .select(Order::getCreateTime, Order::getOrderSN, Order::getPayAmount)
                .eq(Order::getUserId, userId)
                .eq(Order::getShouStatus, 0)
                .eq(Order::getDelStatus, 0)
                .eq(Order::getPayType, status)
                .eq(Order::getStatus, status)
                .orderByDesc(Order::getCreateTime)
                .page(new Page(current, size), OrderLiteBO.class);

        //按创建时间分类，区别订单
        Map<Date, List<OrderLiteBO>> litesMap = page
                .getRecords()
                .stream()
                .collect(Collectors.groupingBy(OrderLiteBO::getCreateTime));

        //查询订单商品所需订单SN码列表
        List<String> snList = page
                .getRecords()
                .stream()
                .map(OrderLiteBO::getOrderSN)
                .collect(Collectors.toList());

        //获取按订单SN码分组的订单商品列表
        Map<String, List<ItemTipBO>> itemsMap = new MPJLambdaWrapper<ItemTipBO>()
                .select(OrderItem::getProdPic, OrderItem::getProdName, OrderItem::getOrderSN)
                .in(OrderItem::getOrderSN, snList)
                .list()
                .stream()
                .collect(Collectors.groupingBy(ItemTipBO::getOrderSN));

        List<Order2SendDto> result = new ArrayList();
        litesMap.entrySet().forEach(e -> {
            Order2SendDto order = new Order2SendDto();
            order.setCreateTime(e.getKey());

            BigDecimal payAmount = new BigDecimal(0);
            List<Order2SendDto.ItemTipDto> tips = new ArrayList();
            e.getValue().forEach(l -> {
                payAmount.add(l.getPayAmount());

                List<ItemTipBO> itemTipBOS = itemsMap.get(l.getOrderSN());
                itemTipBOS.forEach(i -> {
                    Order2SendDto.ItemTipDto tip = new Order2SendDto.ItemTipDto();
                    tip.setItemPic(i.getItemPic());
                    tip.setItemName(i.getItemName());
                    tips.add(tip);
                });
            });

            order.setPayAmount(payAmount);
            order.setItems(tips);
            order.setItemNum(tips.size());

            result.add(order);
        });

        //TODO 暂时强行返回
        Page<Order2SendDto> toPage = new Page();
        toPage.setRecords(result);
        //TODO 不确定对分页的设值是否正确待定
        toPage.setTotal(result.size());
        toPage.setCurrent(current);
        toPage.setSize(size);

        return Result.success(result);
    }

    //TODO 当同订单、品牌的商品列表过长时，没有对其分页处理

    //TODO  目前按物流id获取订单数量，但实际返回是以同品牌商品为准，可能导致一个物流单子，有多个品牌时，导致响应列表与预期不符。

    /**
     * 已发货、已完成的订单列表
     *
     * @param status  订单状态：2.已发货；3.已完成
     * @param userId  用户id
     * @param current 页码
     * @param size    条数
     * @return
     */
    private Result<Page> sendList(Long userId, Integer status, Integer current, Integer size) {
        //获取订单id列表
        List<String> orderIds = new MPJLambdaWrapper<String>()
                .select(Order::getId)
                .eq(Order::getDelStatus, 0)
                .eq(Order::getUserId, userId)
                .eq(Order::getStatus, status)
                .list();

        if (Objects.isNull(orderIds)) {
            return Result.failed("no found order");
        }

        //获取订单商品列表
        MPJLambdaWrapper wrap = new MPJLambdaWrapper<OrderSendBO>()
                .select(OrderItem::getSendId, OrderItem::getProdBrand, OrderItem::getProdPic)
                .select(OrderSend::getStatus, OrderSend::getCreateTime)
                .select(OrderItem::getProdName, OrderItem::getPayPrice, OrderItem::getBrandId)

                .leftJoin(OrderItem.class, OrderItem::getSendId, OrderSend::getId)
                .in(OrderSend::getOrderId, orderIds)
                .orderByDesc(Order::getCreateTime);

        Page<OrderSendBO> tmp = selectJoinListPage(new Page(current, size), OrderSendBO.class, wrap);

        List<OrderSendBO> sendBOs = tmp.getRecords();
        if (Objects.isNull(sendBOs)) {
            return Result.failed("no found order.item");
        }

        //获取分组后的订单商品列表
        Map<Date, Map<Long, Map<Long, List<OrderSendBO>>>> tmpGroup = sendBOs.stream().collect(
                Collectors.groupingBy(OrderSendBO::getCreateTime,
                        Collectors.groupingBy(OrderSendBO::getSendId,
                                Collectors.groupingBy(OrderSendBO::getBrandId))));


        List<OrderSendDto> resultList = new ArrayList();
        for (Map.Entry<Date, Map<Long, Map<Long, List<OrderSendBO>>>> outEntry : tmpGroup.entrySet()) {
            for (Map.Entry<Long, Map<Long, List<OrderSendBO>>> midEntry : outEntry.getValue().entrySet()) {
                //初始化订单物流实例并赋值
                OrderSendDto result = new OrderSendDto();
                result.setSendId(midEntry.getKey());
                result.setCreateTime(outEntry.getKey());

                List<OrderSendDto.SendProdDto> prodDtos = new ArrayList();
                List<BigDecimal> prices = new ArrayList();
                for (Map.Entry<Long, List<OrderSendBO>> nowEntry : midEntry.getValue().entrySet()) {
                    //订单物流实例的简单数据赋值
                    if (Objects.isNull(result.getBrandId())) {
                        result.setBrandId(nowEntry.getKey());

                        OrderSendBO neoBO = nowEntry.getValue().get(0);
                        result.setProdBrand(neoBO.getProdBrand());
                        result.setStatus(neoBO.getStatus());
                    }

                    //订单物流实例的商品列表赋值、价格列表赋值
                    nowEntry.getValue().forEach(bo -> {
                        OrderSendDto.SendProdDto dto = new OrderSendDto.SendProdDto();
                        dto.setProdPic(bo.getProdPic());
                        dto.setProdName(bo.getProdName());

                        prodDtos.add(dto);
                        prices.add(bo.getPayPrice());
                    });
                }

                //订单物流实例的金额、商品数量赋值
                BigDecimal payAmount = prices.stream().reduce(new BigDecimal(0), BigDecimal::add);
                result.setPayAmount(payAmount);
                result.setSendNum(prodDtos.size());

                resultList.add(result);
            }
        }

        //返回值赋值并返回
        Page<OrderSendDto> result = new Page();
        result.setRecords(resultList);
        //TODO 不确定对分页的设值是否正确待定
        result.setTotal(resultList.size());
        result.setCurrent(current);
        result.setSize(size);
        return Result.success(result);
    }

    @Override
    public Result<OrderDetailDto> detail(Long orderId) {
        //获取订单详情
        OrderDetailDto dto = new MPJLambdaWrapper<OrderDetailDto>()
                .select(Order::getId, Order::getPayAmount, Order::getOrderSN)
                .select(Order::getPayType, Order::getPayTime, Order::getCreateTime)
                //TODO
//                .select(Order::getSendCompany, Order::getShouName, Order::getShouPhone)
                .select(Order::getShouDetail)//TODO 暂时收货地址就以详细地址为准
                .eq(Order::getId, orderId)
                .one();

        if (Objects.isNull(dto)) {
            return Result.failed("no order data");
        }

        //获取订单商品列表并赋值
        MPJLambdaWrapper<OrderDetailDto.OrderItemDto> wrap = new MPJLambdaWrapper(OrderItem.class);
        List<OrderDetailDto.OrderItemDto> items = wrap
                .select(OrderItem::getProdPic, OrderItem::getProdName, OrderItem::getProdPrice)
                .select(OrderItem::getProdQuantity, OrderItem::getProdSkuJson)
                .in(OrderItem::getOrderId, orderId)
                .list();
        dto.setItems(items);

        if (Objects.isNull(items)) {
            return Result.failed("no order-item data");
        }

        //商品促销活动减去的金额赋值
        BigDecimal totalAmount = items.stream().map(OrderDetailDto.OrderItemDto::getProdPrice).reduce(new BigDecimal(0), BigDecimal::add);
        BigDecimal lowGap = totalAmount.subtract(dto.getPayAmount());
        dto.setLowGap(lowGap);

        //商品品牌名称赋值
        //TODO
//        items.get(0).get
        return null;
    }

    /**
     * 商品是否有库存
     *
     * @param dtos   购物车商品列表
     * @param lowMap 购物车商品促销映射
     * @return key：isStock：订单商品有无库存；prodName：订单有商品无库存，该键方可使用。
     */
    private Map<String, Object> hasStock(List<CartDto> dtos, Map<Long, CartLowDto> lowMap) {
        Map<String, Object> result = new HashMap();
        String isStockKey = "isStock";//订单商品有无库存
        String nameKey = "prodName";//订单有商品无库存，该键方可使用。

        for (CartDto dto : dtos) {
            CartLowDto low = lowMap.get(dto.getProdId());
            boolean isNull = Objects.isNull(low.getRealStock());
            if (isNull) {
                result.put(isStockKey, false);
                result.put(nameKey, dto.getProdName());
                return result;
            } else {
                Integer stock = low.getRealStock();
                if (stock <= 0 || stock < dto.getQuantity()) {
                    result.put(isStockKey, false);
                    result.put(nameKey, dto.getProdName());
                    return result;
                }
            }
        }
        result.put(isStockKey, true);
        return result;
    }

    /**
     * 订单商品-实际支付金额赋值
     *
     * @param items
     */
    private void initPayPrice(List<OrderItem> items) {
        items.forEach(i -> {
            BigDecimal tmp = i.getProdPrice()
                    .subtract(i.getLowGap());
            i.setPayPrice(tmp);
        });
    }

    /**
     * 锁定订单商品库存
     *
     * @param dtos 购物车商品列表
     */
    private void lockStock(List<CartDto> dtos) {
        Map<Long, Integer> skuIds = new HashMap();//key：skuId；value：quantity
        dtos.forEach(l ->
                skuIds.put(l.getSkuId(), l.getQuantity()));

        //TODO 目前该方法无脑尽少单条SQL查

        List<Sku> skus = new MPJLambdaWrapper<Sku>(Sku.class)
                .selectAll(Sku.class)
                .in(Sku::getId, skuIds.keySet())
                .list();

        skus.forEach(s -> {
            Integer lockStock = s.getLockStock();
            Integer quantity = skuIds.get(s.getId());
            s.setLockStock(lockStock + quantity);
        });

        skuService.updateBatchById(skus);
    }

    /**
     * 计算订单总金额
     *
     * @param items 订单商品列表
     * @return
     */
    private BigDecimal calcTotalAmount(List<OrderItem> items) {
        BigDecimal total = new BigDecimal(0);

        for (OrderItem item : items) {
            BigDecimal quantity = new BigDecimal(item.getProdQuantity());
            BigDecimal tmpPrice = item.getProdPrice().multiply(quantity);
            total = total.add(tmpPrice);
        }

        return total;
    }

    /**
     * 计算促销金额
     *
     * @param items  订单商品列表
     * @param lowMap key：商品id；value：购物车商品促销信息
     * @return
     */
    private BigDecimal calcLowAmount(List<OrderItem> items, Map<Long, CartLowDto> lowMap) {
        BigDecimal total = new BigDecimal(0);

        for (OrderItem item : items) {
            CartLowDto lowTmp = lowMap.get(item.getProdId());
            BigDecimal gap = lowTmp.getGap();
            if (Objects.nonNull(gap)) {//TODO 可能不需要这个非空检测？
                BigDecimal quantity = new BigDecimal(item.getProdQuantity());
                BigDecimal tmpPrice = gap.multiply(quantity);
                total = total.add(tmpPrice);
            }
        }

        return total;
    }

    /**
     * 促销信息json赋值
     *
     * @param lowTmp 购物车商品促销信息列表
     * @return
     */
    private String initMsgJson(List<CartLowDto> lowTmp) {
        List<String> jsonList = new ArrayList();

        for (CartLowDto lowDto : lowTmp) {
            jsonList.add(lowDto.getLowMsg());
        }

        return JSON.toJSONString(jsonList);
    }

    /**
     * 计算实际支付金额
     *
     * @param order 订单
     * @return
     */
    private BigDecimal calcPayAmount(Order order) {
        return order.getTotalAmount()
                .add(order.getSendAmount())
                .subtract(order.getLowAmount());
    }

    /**
     * 下单的商品从购物车中清除
     *
     * @param prodIds 商品id列表
     * @param userId  用户id
     */
    private boolean clearCartByOrder(Set<Long> prodIds, Long userId) {
        return cartService.update()
                .set("delete_status", 2)
                .eq("user_id", userId)
                .in("product_id", prodIds)
                .update();
    }
}