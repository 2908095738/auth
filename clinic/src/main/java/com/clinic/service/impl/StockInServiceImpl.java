package com.clinic.service.impl;

import cn.hutool.db.DbRuntimeException;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clinic.dto.QueryStockInDto;
import com.clinic.dto.param.PutStockList;
import com.clinic.dto.param.PutStockParam;
import com.clinic.dto.param.QueryStockInParam;
import com.clinic.entity.StockIn;
import com.clinic.entity.StockInDrug;
import com.clinic.mapper.StockInMapper;
import com.clinic.service.StockInService;
import com.clinic.util.LoginUser;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.alibaba.fastjson2.JSON.toJSONString;

/**
* @author 路晨霖
* @description 针对表【stock_in(库存：入库)】的数据库操作Service实现
* @createDate 2023-09-20 08:29:40
*/
@Slf4j
@Service
public class StockInServiceImpl extends ServiceImpl<StockInMapper, StockIn>
    implements StockInService{

    @Override
    public StockIn saveBatch(String no, PutStockList param) throws DbRuntimeException {
        Long uid = LoginUser.getId();
        StockIn stockIn = new StockIn(null, no, param.getTotalCost(), uid, param.getRemark(), null);
        if(save(stockIn)) return stockIn;
        log.error("药品入库【入库单】异常! stockInService::save(stockIn={}; param={}))", toJSONString(stockIn), toJSONString(param));
        throw new DbRuntimeException("药品入库【入库单】异常！");
    }

    @Override
    public Long save(String no, PutStockParam param, Long uid) throws DbRuntimeException {
        StockIn stockIn = new StockIn(null, no, param.getTotalCost(), uid, param.getRemark(), null);
        if(save(stockIn)) return stockIn.getId();
        log.error("药品入库【入库单】异常! stockInService::save(stockIn={}; param={}))", toJSONString(stockIn), toJSONString(param));
        throw new DbRuntimeException("药品入库【入库单】异常！");
    }


    @Override
    public Page<QueryStockInDto> query(QueryStockInParam param) {
        return baseMapper.selectJoinPage(param.toPage(), QueryStockInDto.class, new MPJLambdaWrapper<StockIn>()
                .selectAll(StockIn.class)
                .selectCollection(StockInDrug.class,QueryStockInDto::getStockInDrugs)
                .leftJoin(StockInDrug.class,StockInDrug::getStockInId,StockIn::getId)
                .eq(StockIn::getUserId,LoginUser.getId())
        );
    }
}




