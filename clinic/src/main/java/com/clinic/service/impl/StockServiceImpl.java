package com.clinic.service.impl;

import cn.hutool.db.DbRuntimeException;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clinic.dto.param.PutStockList;
import com.clinic.entity.Settings;
import com.clinic.entity.Stock;
import com.clinic.entity.StockBatch;
import com.clinic.entity.StockUnit;
import com.clinic.enums.DrugTypeEnum;
import com.clinic.enums.StockStateCountTypeEnum;
import com.clinic.enums.StockStateEnum;
import com.clinic.mapper.StockMapper;
import com.clinic.service.SettingsService;
import com.clinic.service.StockBatchService;
import com.clinic.service.StockService;
import com.clinic.service.StockUnitService;
import com.clinic.util.LoginUser;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
* @author 路晨霖
* @description 针对表【stock(库存)】的数据库操作Service实现
* @createDate 2023-09-20 08:28:13
*/
@Slf4j
@Service
public class StockServiceImpl extends ServiceImpl<StockMapper, Stock>
    implements StockService{

    private final StockUnitService stockUnitService;

    private final StockBatchService batchService;

    private final SettingsService settingsService;


    @Override
    public void saveBatch(PutStockList param) throws DbRuntimeException {
        Long uid = LoginUser.getId();

        Map<Stock, List<StockBatch>> oldStock = new HashMap<>(), newStock = new HashMap<>();

        List<StockBatch> stockBatches = batchService.searchByApprovalNumbers(param.getApprovalNumbers(), param.getBatchNumbers());
        Map<String, StockBatch> approvalNumberMap = new HashMap<>(stockBatches.size());
        stockBatches.forEach(stockBatch -> approvalNumberMap.put(stockBatch.getApprovalNumber(), stockBatch));

        // 增、存量
        param.getDrugs().forEach(inputDrug -> inputDrug.getBatchList().forEach(inputBatch -> {
            inputBatch.setType(DrugTypeEnum.map.get(inputBatch.getTypeName()).getCode());
            StockBatch stockBatch = approvalNumberMap.get(inputBatch.getApprovalNumber());
            List<StockBatch> stockBatchList = new ArrayList<>();
            if(nonNull(stockBatch)) {
                oldStock.putIfAbsent(inputDrug, stockBatchList);
                stockBatchList = oldStock.get(inputDrug);
                Long stockNumber = inputBatch.getNumber() + stockBatch.getNumber();

                inputBatch
                        .setId(stockBatch.getId())
                        .setNumber(stockNumber)
                        .setTotalNumber(stockNumber)
                        .setUserId(uid)
                ;

                stockBatchList.add(inputBatch);
            } else {
                newStock.putIfAbsent(inputDrug, stockBatchList);
                stockBatchList = newStock.get(inputDrug);
                inputBatch
                        .setTotalNumber(inputBatch.getNumber())
                        .setUserId(uid);
                stockBatchList.add(inputBatch);
            }
        }));
        // 存量处理
        boolean oldStockResult = true, newStockResult = true;
        if(MapUtils.isNotEmpty(oldStock)) {
            List<StockBatch> needUpdateOldBatch = oldStock.values().stream().flatMap(Collection::stream).distinct().collect(Collectors.toList());
            batchService.updateBatchById(needUpdateOldBatch);
        }

        // 增量处理
        if(MapUtils.isNotEmpty(newStock) && saveBatch(newStock.keySet())) {
            // 将 Stock 自增 ID 设置到每个 StockBatch，合并为一个大 List
            List<StockBatch> needSaveNewBatchList = newStock.entrySet().stream().map(entry ->
                    entry.getValue().stream().map(
                            stockBatch -> stockBatch.setStockId(entry.getKey().getId())
                    ).collect(Collectors.toList())
            ).flatMap(Collection::stream).distinct().collect(Collectors.toList());
            newStockResult = batchService.saveBatch(needSaveNewBatchList) && stockUnitService.saveBatch(
                    needSaveNewBatchList.stream().map(newBatch -> {
                        List<StockUnit> unitList = newBatch.getStockUnitList();
                        unitList.forEach(unit -> unit.setBatchId(newBatch.getId()));
                        return unitList;
                    }).flatMap(Collection::stream).distinct().collect(Collectors.toList())
            );
        }

        if(!oldStockResult || !newStockResult) {
            throw new DbRuntimeException("药品入库【库存修改 & 新增】异常！");
        }
    }


    @Override
    public void countStockState(StockBatch batch, Settings settings){
        if(isNull(settings) || isNull(settings.getCountVal())||isNull(settings.getStateCountRule())){
            batch.setState(StockStateEnum.UNDEFINED.getCode());
        } else {
            batch.setState(computeStockNumberState(batch, settings));
        }
    }

    public Integer computeStockNumberState(StockBatch batch, Settings settings) {
        return stockNumberStateIsNormal(
                batch.getNumber(),
                StockStateCountTypeEnum.map.get(settings.getStateCountRule()),
                batch.getTotalNumber(),
                settings.getCountVal()) ? 0 : 1;
    }

    /**
     * 库存数量是否正常
     * @return 是否异常
     */
    @Override
    public Boolean stockNumberStateIsNormal(Long stockNumber, StockStateCountTypeEnum countType, Long totalNumber, Integer contVal) {
        boolean flag = false;
        switch (countType){
            case PERCENTAGE:
                if(stockNumber > 0) flag = stockNumber.equals(totalNumber) || ((double) stockNumber % totalNumber) * 100 > contVal;
                break;
            case NUMBER:
                flag = stockNumber > contVal;
                break;
        }
        return flag;
    }

    @Override
    public Stock searchByName(Stock stockParam) {
        return lambdaQuery().eq(Stock::getName, stockParam.getName()).one();
    }

    @Override
    public Boolean stockNumberStateIsNotNormal(StockBatch stockBatch) {
        CountSetting countSetting = CountSetting.create(stockBatch);

        if(trySetCountSettingIfAbsent(countSetting)) initCountSettingIfAbsent(countSetting);

        return !stockNumberStateIsNormal(
                stockBatch.getNumber(),
                StockStateCountTypeEnum.map.get(countSetting.countRule),
                stockBatch.getTotalNumber(),
                countSetting.countVal
        );
    }

    /**
     * 当统计设置为空时，尝试设置
     * @param countSetting 统计设置（统计类型， 统计值）
     * @return 设置是否失败
     */
    private Boolean trySetCountSettingIfAbsent(CountSetting countSetting) {
        if(isNull(countSetting.countRule) || isNull(countSetting.countVal) ) {
            Settings settings = settingsService.getByUserId();
            if(nonNull(settings)) {
                countSetting.countRule = settings.getStateCountRule();
                countSetting.countVal = settings.getCountVal();
                return false;
            }
        }
        return true;
    }

    /**
     * 当统计设置为空时，尝试初始化
     * @param countSetting 统计设置（统计类型， 统计值）
     */
    private void initCountSettingIfAbsent(CountSetting countSetting) {
        if(isNull(countSetting.countRule) || isNull(countSetting.countVal) ) {
            countSetting.countRule = StockStateCountTypeEnum.PERCENTAGE.getCode();
            countSetting.countVal = 20;
        }
    }

    @AllArgsConstructor
    private static class CountSetting {
        private Integer countRule;
        private Integer countVal;

        public static CountSetting create(StockBatch stockBatch) {
            return new CountSetting(stockBatch.getStateCountRule(), stockBatch.getCountVal());
        }
    }


    @Autowired
    public StockServiceImpl(StockUnitService stockUnitService, StockBatchService batchService, SettingsService settingsService) {
        this.stockUnitService = stockUnitService;
        this.batchService = batchService;
        this.settingsService = settingsService;
    }
}




