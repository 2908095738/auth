package com.clinic.app;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bbs.Result;
import com.clinic.dto.PrescriptionDrugDto;
import com.clinic.dto.PrescriptionDto;
import com.clinic.dto.param.PutStock;
import com.clinic.dto.param.PutStockList;
import com.clinic.dto.param.QueryStockInParam;
import com.clinic.dto.param.StockSearchParam;
import com.clinic.entity.Settings;
import com.clinic.entity.Stock;
import com.clinic.entity.StockBatch;
import com.clinic.entity.StockIn;
import com.clinic.entity.StockInDrug;
import com.clinic.entity.StockUnit;
import com.clinic.entity.Unit;
import com.clinic.enums.DrugExpiryStateEnum;
import com.clinic.enums.DrugTypeEnum;
import com.clinic.enums.StockStateEnum;
import com.clinic.mapper.StockMapper;
import com.clinic.service.SettingsService;
import com.clinic.service.StockBatchService;
import com.clinic.service.StockInDrugService;
import com.clinic.service.StockInService;
import com.clinic.service.StockService;
import com.clinic.util.LoginUser;
import com.clinic.util.RedisUtil;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.clinic.enums.DrugExpiryStateEnum.ABOUT_EXPIRES;
import static com.clinic.enums.DrugExpiryStateEnum.EXPIRES;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.math.NumberUtils.*;

@Slf4j
@Service
public class AppStockService extends ServiceImpl<StockMapper, Stock> {

    private RedisUtil redis;

    private StockInService stockInService;

    private StockInDrugService stockInDrugService;

    private StockService stockService;

    private DataSourceTransactionManager transactionManager;

    private TransactionDefinition transactionDefinition;

    private StockBatchService batchService;

    private SettingsService settingsService;


    private static final String STOCK_NO_GENERATE = "STOCK_NO_GENERATE";

    @Value("${setting.stock.expiry.alert.month}")
    private Integer stockDefaultExpiryAlertMonth;

    public Result search(StockSearchParam param) {
        if(nonNull(param.getId())) {
            Optional<StockBatch> stockBatch = searchById(param);
            if(stockBatch.isPresent()) return Result.success(stockBatch.get());
        }

        if(isNotBlank(param.getApprovalNumber())) {
            StockBatch stockBatch = batchService.searchByApprovalNumber(param.getApprovalNumber());
            if(nonNull(stockBatch)) return Result.success(stockBatch);
        }
        MPJLambdaWrapper<Stock> wrapper = getBaseWrapper()
                .eq(isNotBlank(param.getManufacturerName()), StockBatch::getManufacturer, param.getManufacturerName())
                .eq(isNotBlank(param.getBatchNumber()), StockBatch::getBatchNumber, param.getBatchNumber())
                .eq(nonNull(param.getType()), StockBatch::getType, param.getType())
                .eq(isNotBlank(param.getDosageForm()), StockBatch::getDosageForm, param.getDosageForm())
                .eq(nonNull(param.getProduceDate()), StockBatch::getProduceDate, param.getProduceDate())

                .eq(isNotBlank(param.getName()), Stock::getName, param.getName())
                .or()
                .eq(isNotBlank(param.getName()), Stock::getAlias, param.getName())

                .between(
                        isNull(param.getProduceDate()) && nonNull(param.getProduceStartDate()) && nonNull(param.getProduceEndDate()),
                        StockBatch::getProduceDate,
                        param.getProduceStartDate(),
                        param.getProduceEndDate()
                )
                .eq(nonNull(param.getExpiryDate()), StockBatch::getExpiryDate, param.getExpiryDate())
                .between(
                        isNull(param.getExpiryDate()) && nonNull(param.getExpiryStartDate()) && nonNull(param.getExpiryEndDate()),
                        StockBatch::getExpiryDate,
                        param.getExpiryStartDate(),
                        param.getExpiryEndDate()
                )
        ;
        List<Stock> stocks = baseMapper.selectJoinList(Stock.class, wrapper);
        return Result.success(countStockStateAndPage(param.toPage(), stocks));
    }


    private MPJLambdaWrapper<Stock> getBaseWrapper(){
        return new MPJLambdaWrapper<Stock>()
                .selectAll(Stock.class)
                .selectCollection(StockBatch.class, Stock::getBatchList, batch -> batch
                        .collection(StockInDrug.class, StockBatch::getStockInDrugList)
                        .collection(StockUnit.class, StockBatch::getStockUnitList, stockUnit -> stockUnit
                                .collection(Unit.class, StockUnit::getUnitList)))
                .leftJoin(StockBatch.class, StockBatch::getStockId, Stock::getId)
                .leftJoin(StockUnit.class, StockUnit::getBatchId, StockBatch::getId)
                .leftJoin(Unit.class, Unit::getId, StockUnit::getUnitId)
                .leftJoin(StockInDrug.class, StockInDrug::getApprovalNumber, StockBatch::getApprovalNumber)
                .eq(StockBatch::getUserId, LoginUser.getId());
    }

    public Result queryStockIn(QueryStockInParam param) {
        return Result.success(stockInService.query(param));
    }


    private Optional<StockBatch> searchById(StockSearchParam param) {
        return Optional.of(param.getId()).map(batchService::getById);
    }

    private Page countStockStateAndPage(Page<Stock> page, List<Stock> records) {
        Settings settings = settingsService.getByUserId();
        Integer stockExpiryAlertMonth = getUserSettingStockExpiryAlertMonth(settings);  //用户设置的库存药品过期提醒时间
        List<Stock> stocks = records.stream()
                .skip((page.getCurrent() - 1) * page.getSize())
                .limit(page.getSize())
                .collect(Collectors.toList());
        stocks.forEach(stock -> stock.getBatchList().forEach(batchDrug -> {

            batchDrug.setExpiryState(computeDrugIsExpiry(batchDrug, stockExpiryAlertMonth).getCode());

            DrugTypeEnum typeEnum = DrugTypeEnum.values()[batchDrug.getType()];
            batchDrug.setTypeObj(typeEnum);
            stockService.countStockState(batchDrug, settings);
        }));

        page.setTotal(records.size());
        page.setRecords(stocks);
        return page;
    }

    private DrugExpiryStateEnum computeDrugIsExpiry(StockBatch batchDrug, Integer stockExpiryAlertMonth) {
        long betweenDay = DateUtil.between(new Date(), batchDrug.getExpiryDate(), DateUnit.DAY, false);
        // >0 时间线：nowDate -> expiryDate（当参数 1 为当前日期，且差值 > 0 时，未过期）
        if(betweenDay == INTEGER_ZERO) {
            return ABOUT_EXPIRES;
        } else if(betweenDay > INTEGER_ZERO) {
            if(betweenDay > 30 && betweenDay / 30 > stockExpiryAlertMonth) {
                return DrugExpiryStateEnum.NORMAL;
            }
        }
        return EXPIRES;
    }

    private Integer getUserSettingStockExpiryAlertMonth (Settings settings) {
        return nonNull(settings) && nonNull(settings.getExpiryAlertMonth()) ? settings.getExpiryAlertMonth() : stockDefaultExpiryAlertMonth;
    }


    public Result<Boolean> putStock(PutStock param) {
        StockBatch batch = batchService.searchBatchAndUnitByID(param.getId());
        if(isNull(batch)) return Result.failed(400, "用户 ID 不存在");

        long number = 0L;
        if(Objects.equals(batch.getUnitId(), param.getUnitId())) {
            number = param.getNumber() + batch.getNumber();
        } else {
            List<Unit> units = batch.getStockUnitList().stream()
                    .sorted(Comparator.comparingInt(StockUnit::getSort))
                    .map(StockUnit::getUnit).collect(Collectors.toList());

            List<StockUnit> stockUnitList = batch.getStockUnitList();

            for (int index = 0; index < stockUnitList.size(); index++) {
                Unit item = stockUnitList.get(index).getUnit();
                if(Objects.equals(param.getUnitId(), item.getId())) {
                    number = param.getNumber();
                    stockUnitList = stockUnitList.subList(index + 1, units.size());
                    break;
                }
            }

            for (StockUnit stockUnit : stockUnitList) {
                number = number * stockUnit.getStepSize();
            }
            number += batch.getNumber();
        }
        return Result.of(batchService.updateById(new StockBatch(param.getId(), number)));
    }

    public Result putStockList(PutStockList param) {
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        String no = generateStockInNO();


        Map<String, List<StockBatch>> drugNameAndStockMap = param.getBatchList().stream().collect(Collectors.groupingBy(StockBatch::getName));

        List<Stock> drugs = drugNameAndStockMap.entrySet().stream()
                .map(entry -> new Stock(LoginUser.getId(), entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        param.setDrugs(drugs);

        try {
            stockService.saveBatch(param);
            StockIn stockIn = stockInService.saveBatch(no, param);
            stockInDrugService.saveBatch(no, param, stockIn);
            transactionManager.commit(transaction);
            return Result.success();
        } catch (RuntimeException e) {
            transactionManager.rollback(transaction);
            e.printStackTrace();
            return Result.failed();
        }
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class DrugExpiryCount {

        private Long normal;

        private Long aboutExpires;

        private Long expires;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DrugExpiryGroup {

        private List<StockBatch> normal;

        private List<StockBatch> aboutExpires;

        private List<StockBatch> expires;
    }

    public DrugExpiryGroup countAndUpdateDrugExpiryState(){
        Settings settings = settingsService.getByUserId();
        Integer stockExpiryAlertMonth = getUserSettingStockExpiryAlertMonth(settings);  //用户设置的库存药品过期提醒时间'

        List<StockBatch> allDrugBatch = batchService.lambdaQuery()
                .eq(StockBatch::getUserId, LoginUser.getId())
                .list();

        List<StockBatch> expiresStateNormal = new ArrayList<>();    // 正常
        List<StockBatch> needUpdateToExpiresState = new ArrayList<>();  // 过期（需要 update DB）
        List<StockBatch> needUpdateToAboutExpiresState = new ArrayList<>(); // 即将过期（需要 update DB）
        allDrugBatch.forEach(drugBatch -> {
            if(
                    DrugExpiryStateEnum.NORMAL.getCode().equals(drugBatch.getExpiryState()) ||
                    ABOUT_EXPIRES.getCode().equals(drugBatch.getExpiryState())
            ) {
                DrugExpiryStateEnum expiryState = computeDrugIsExpiry(drugBatch, stockExpiryAlertMonth);
                if(EXPIRES.equals(expiryState)) {
                    drugBatch.setExpiryState(EXPIRES.getCode());
                    needUpdateToExpiresState.add(drugBatch);
                } else if(ABOUT_EXPIRES.equals(expiryState)) {
                    drugBatch.setExpiryState(ABOUT_EXPIRES.getCode());
                    needUpdateToAboutExpiresState.add(drugBatch);
                } else {
                    expiresStateNormal.add(drugBatch);
                }
            }
        });
        if(needUpdateToExpiresState.size() > INTEGER_ZERO) {
            batchService.updateBatchById(needUpdateToExpiresState);
        }
        if(needUpdateToAboutExpiresState.size() > INTEGER_ZERO) {
            batchService.updateBatchById(needUpdateToAboutExpiresState);
        }
        return new DrugExpiryGroup(expiresStateNormal, needUpdateToExpiresState, needUpdateToAboutExpiresState);
    }



    public boolean updateNum(PrescriptionDto prescriptionDto) {
        //校验库存不为零

        List<PrescriptionDrugDto> drugList = prescriptionDto.getDrugList();
        List<Long> stockBatchIds = drugList.stream().map(PrescriptionDrugDto::getStockBatchId).collect(Collectors.toList());
        Map<Long, StockBatch> stockBatchMap = batchService.lambdaQuery().in(StockBatch::getId, stockBatchIds).list().stream().collect(Collectors.toMap(StockBatch::getId, o2 -> o2));
        List<StockBatch> newStockDrugList = drugList.stream().map(drug->{
            StockBatch oldStockBatch = stockBatchMap.get(drug.getStockBatchId());
            return new StockBatch(drug,oldStockBatch.getNumber());
        }).collect(Collectors.toList());
        return batchService.updateBatchById(newStockDrugList);
    }




    /**
     * 创建入库编号
     * @return 入库编号
     */
    private String generateStockInNO() {
        return String.valueOf(
                LoginUser.getId()) +
                System.currentTimeMillis() +
                redis.increment(STOCK_NO_GENERATE)
        ;
    }

    @Autowired
    public AppStockService(RedisUtil redis, StockInService stockInService, StockInDrugService stockInDrugService, StockService stockService, DataSourceTransactionManager transactionManager, TransactionDefinition transactionDefinition, StockBatchService batchService, SettingsService settingsService) {
        this.redis = redis;
        this.stockInService = stockInService;
        this.stockInDrugService = stockInDrugService;
        this.stockService = stockService;
        this.transactionManager = transactionManager;
        this.transactionDefinition = transactionDefinition;
        this.batchService = batchService;
        this.settingsService = settingsService;
    }

}
