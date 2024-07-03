package com.clinic.cache.prescription;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.clinic.cache.unit.UnitCache;
import com.clinic.dto.vo.PrescriptionSearchDrugVO;
import com.clinic.entity.StockBatch;
import com.clinic.entity.StockUnit;
import com.clinic.entity.Unit;
import com.clinic.service.PrescriptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import static java.lang.Thread.sleep;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;



@Component
@Slf4j
public class CureCache implements SearchPrescriptionDrug {


    private final Redis redis;

    private final DataBase dataBase;

    private final PrescriptionService prescriptionService;

    private final DataSourceTransactionManager transactionManager;

    private final TransactionDefinition transactionDefinition;

    private final UnitCache cache;

    @Override
    public Result<Page<PrescriptionSearchDrugVO>> search(String drugName) throws InterruptedException {
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        Result<Page<PrescriptionSearchDrugVO>> result;
        try {
            int tryNumber = 3;
            while (tryNumber > 0) {
                Object idMapObj = redis.searchIdMapping(drugName);
                if(Method.exists(idMapObj)) {
                    result = getByCache(idMapObj);
                    transactionManager.commit(transaction);
                    return result;
                }
                if(redis.tryAcquire()) {
                    result = getByDB(drugName);
                    redis.tryRelease();
                    transactionManager.commit(transaction);
                    return result;
                }
                tryNumber--;
                sleep(500);
            }
            result = getByDB(drugName);
            transactionManager.commit(transaction);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        transactionManager.rollback(transaction);
        return Result.failedNull();
    }

    private Result<Page<PrescriptionSearchDrugVO>> getByCache(Object idMapObj) {
        List<Long> ids = Converter.toStockIds(idMapObj);
        List<PrescriptionSearchDrugVO> vos = redis.searchStockMapping(ids);

        // 筛选 Redis 中不存在 VO 的 ID，提取 ID 批量从数据库加载填充
        List<Integer> emptyStockIndexList = new ArrayList<>(ids.size());
        List<Long> emptyStockIds = new ArrayList<>(ids.size());
        for (int index = 0; index < vos.size(); index++) {
            PrescriptionSearchDrugVO vo = vos.get(index);
            if(isNull(vo)) {
                Long id = ids.get(index);
                emptyStockIds.add(id);
                emptyStockIndexList.add(index);
            }
        }
        if(emptyStockIds.size() > 0) {
            List<PrescriptionSearchDrugVO> emptyStock = dataBase.search(emptyStockIds);
            for (int index = 0; index < emptyStock.size(); index++) {
                Integer emptyStockIndex = emptyStockIndexList.get(index);
                PrescriptionSearchDrugVO emptyStockVO = emptyStock.get(index);
                vos.set(emptyStockIndex, emptyStockVO);
            }
        }
        return Result.success(Method.toPage(vos));
    }

    private Result<Page<PrescriptionSearchDrugVO>> getByDB(String drugName) {
        List<StockBatch> stockBatchList = prescriptionService.searchStockBatch(drugName).getRecords();//纯 SQL JOIN


        List<Long> ids = new ArrayList<>(10);
        List<PrescriptionSearchDrugVO> vos = stockBatchList.stream().map(stockBatch -> {


            PrescriptionSearchDrugVO vo = prescriptionService.converter(stockBatch);


            List<StockUnit> stockUnitList = stockBatch.getStockUnitList();
            List<String> idStrList = stockUnitList.stream().map(stockUnit -> cache.generateKey(stockUnit.getUnitId())).collect(Collectors.toList());
            List<String> unitStrList = cache.getUnitStrList(idStrList);
            List<PrescriptionSearchDrugVO.Unit> units = new ArrayList<>(unitStrList.size());
            for (int index = 0; index < unitStrList.size(); index++) {
                Unit unit = JSONUtil.toBean(unitStrList.get(index), Unit.class);
                units.add(new PrescriptionSearchDrugVO.Unit(unit.getId(), unit.getName(), stockUnitList.get(index).getStepSize()));
            }
            vo.setUnits(units);
            vo.setMaxUnit(units.get(vo.getUnits().size() - 1));
            vo.setMinUnit(units.get(0));

            ids.add(vo.getId());
            return vo;
        }).collect(Collectors.toList());
        redis.loadMapping(drugName, ids, vos);   //保存映射到 Redis

        Page<PrescriptionSearchDrugVO> page = new Page<>(1, 10);
        page.setRecords(vos);
        return Result.success(page);
    }

    @Autowired
    public CureCache(Redis redis, DataBase dataBase, PrescriptionService prescriptionService, DataSourceTransactionManager transactionManager, TransactionDefinition transactionDefinition, UnitCache cache) {
        this.redis = redis;
        this.dataBase = dataBase;
        this.prescriptionService = prescriptionService;
        this.transactionManager = transactionManager;
        this.transactionDefinition = transactionDefinition;
        this.cache = cache;
    }

    public static class Converter {

        public static PrescriptionSearchDrugVO toVO(Object stockObj) {
            return nonNull(stockObj) ? JSONUtil.toBean((String) stockObj, PrescriptionSearchDrugVO.class) : null;
        }

        public static List<Long> toStockIds(Object idMapObj) {
            return JSONUtil.toBean((String) idMapObj, new TypeReference<List<Long>>(){}, true);
        }
    }

    private static class Method {

        private static Boolean exists(Object idMapObj) {
            return nonNull(idMapObj);
        }

        private static Page<PrescriptionSearchDrugVO> toPage(List<PrescriptionSearchDrugVO> voList) {
            Page<PrescriptionSearchDrugVO> page = new Page<>(1, 10);
            page.setRecords(voList);
            return page;
        }
    }
}
