package com.clinic.app;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bbs.Result;
import com.clinic.dto.DeficiencyStockVo;
import com.clinic.dto.ExpiryDateStockVo;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

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
        Integer stockExpiryAlertMonth = nonNull(settings) && nonNull(settings.getExpiryAlertMonth()) ? settings.getExpiryAlertMonth() : stockDefaultExpiryAlertMonth;
        Date alertDate = getAlertDate(stockExpiryAlertMonth);

        List<Stock> stocks = records.stream()
                .skip((page.getCurrent() - 1) * page.getSize())
                .limit(page.getSize())
                .collect(Collectors.toList());
        stocks.forEach(stock -> stock.getBatchList().forEach(batch -> {

            batch.getStockInDrugList().sort((stockInDrug1, stockInDrug2) -> DateUtil.compare(stockInDrug1.getCreateTime(), stockInDrug2.getCreateTime()));
            batch.setExpiryState(batch.getExpiryDate().compareTo(alertDate) > 0? StockStateEnum.NORMAL.getCode():StockStateEnum.SHORTAGE.getCode());
            DrugTypeEnum typeEnum = DrugTypeEnum.values()[batch.getType()];
            batch.setTypeObj(typeEnum);
            stockService.countStockState(batch, settings);
        }));

        page.setTotal(records.size());
        page.setRecords(stocks);
        return page;
    }

    private Date getAlertDate(Integer userAlertMonth){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MONTH, userAlertMonth);
        return calendar.getTime();
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

    public Result<List<Map<String,Object>>> stockDeficiencyAlert(){
        Settings settings = settingsService.getByUserId();
        Date alertDate = getAlertDate(settings.getExpiryAlertMonth());
        MPJLambdaWrapper<Stock> wrapper = getBaseWrapper();
        List<Stock> stocks = baseMapper.selectJoinList(Stock.class, wrapper);

        List<Map<String,Object>> result = new ArrayList<>();
        stocks.forEach(o-> {
            Map<String,Object> resultMap = new HashMap<>();
            o.getBatchList().forEach(batch->{
                                batch.setExpiryState(batch.getExpiryDate().compareTo(alertDate) > 0? StockStateEnum.NORMAL.getCode():StockStateEnum.SHORTAGE.getCode());
                                stockService.countStockState(batch, settings);
            });

            List<DeficiencyStockVo> stockStateList = o.getBatchList().stream().filter(stock -> Objects.equals(stock.getState(), StockStateEnum.SHORTAGE.getCode()))
                    .map(batch -> new DeficiencyStockVo(o.getName(), batch.getBatchNumber(), batch.getNumber())).collect(Collectors.toList());
            List<ExpiryDateStockVo> expiryStateList = o.getBatchList().stream().filter(stock -> stock.getExpiryState() == 1)
                    .map(batch -> new ExpiryDateStockVo(o.getName(), batch.getBatchNumber(), batch.getExpiryDate())).collect(Collectors.toList());

            if(CollectionUtils.isNotEmpty(stockStateList)||CollectionUtils.isNotEmpty(expiryStateList)){
                resultMap.put("name", o.getName());
                if(CollectionUtils.isNotEmpty(stockStateList))resultMap.put("stockStateList",stockStateList);
                if(CollectionUtils.isNotEmpty(expiryStateList))resultMap.put("expiryStateList",expiryStateList);
                result.add(resultMap);
            }
        });
       return Result.success(result);
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
