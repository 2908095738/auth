package com.clinic.app.retail;

import com.bbs.Result;
import com.clinic.converter.RetailConverter;
import com.clinic.dto.param.AddRetailParams;
import com.clinic.entity.RetailDrugRecord;
import com.clinic.entity.RetailRecord;
import com.clinic.entity.StockBatch;
import com.clinic.service.RetailDrugRecordService;
import com.clinic.service.RetailRecordService;
import com.clinic.service.StockBatchService;
import com.clinic.util.LogUtil;
import com.clinic.util.LoginUser;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.util.validation.metadata.DatabaseException;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

import static com.clinic.app.retail.Method.*;

@Slf4j
@RestController
@RequestMapping
public class Add {

    private RetailConverter converter;

    private final RetailRecordService recordService;

    private final RetailDrugRecordService drugRecordService;

    private final StockBatchService stockService;

    private final DataSourceTransactionManager transactionManager;

    private final TransactionDefinition transactionDefinition;

    @Resource(name = "protoStuffTemplate")
    private RedisTemplate<String, String> redis;

    private static final String ADD_RETAIL_KEY = "lock_add_retail_lock";

    @PutMapping("/retail")
    public Result<Boolean> add(@RequestBody @Valid AddRetailParams params) {
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        String lockKey = ADD_RETAIL_KEY;
        try {
            int i = 0;
            while (i < 3) {
                if(tryAcquireLock(lockKey)) {
                    if(!exec(params)) break;
                    transactionManager.commit(transaction);
                    return Result.success();
                }
                i++;
                Thread.sleep(1000);
            }
            if(i == 2) log.warn("try acquire lock failed!");

        } catch (IllegalArgumentException e) {
            transactionManager.rollback(transaction);
            removeLock(lockKey);
            return Result.failed(400, e.getMessage());
        } catch (RuntimeException | InterruptedException e) {
            e.printStackTrace();
        }
        transactionManager.rollback(transaction);
        removeLock(lockKey);
        return Result.failed();
    }

    private Boolean exec(AddRetailParams params) throws DatabaseException {
        RetailRecord retailRecord = converter.toEntity(params);
        retailRecord.setUserId(LoginUser.getId());

        List<RetailDrugRecord> retailDrugRecords = converter.toEntity(params.getDrugList());
        List<StockBatch> stockBatches = stockService.searchWaitUpdateStocks(params);
        if(updateTargetStockIsPresent(params, stockBatches)) {

            List<StockBatch> waitUpdateStockBatch = new ArrayList<>(stockBatches.size());
            for (int index = 0; index < stockBatches.size(); index++) {
                RetailDrugRecord retailDrugRecord = retailDrugRecords.get(index);
                StockBatch stockBatch = stockBatches.get(index);

                retailDrugRecord.fillStockBatchInfo(stockBatch);

                long number = computeSellAfterStockNumber(retailDrugRecord, stockBatch);

                Preconditions.checkArgument(sellAfterStockNumberIsNormal(number), "库存数量不足，无法执行操作");

                stockBatch.setNumber(number);
                waitUpdateStockBatch.add(stockBatch);
            }

            if(!stockService.update(waitUpdateStockBatch)) throw new DatabaseException("库存数量更新失败");

            if(!recordService.save(retailRecord)) throw new DatabaseException("零售记录入库失败");

            fillRetailIdToDrugRecords(retailDrugRecords, retailRecord);

            if(!drugRecordService.saveBatch(retailDrugRecords))  throw new DatabaseException("零售药品记录入库失败");
            LogUtil.Operation.record("零售",LoginUser.get().getName()+"新增一条零售记录：零售记录id="+retailRecord.getId(), Level.INFO);
            return true;
        }
        return false;
    }

    private Boolean tryAcquireLock(String lockKey) {
        return redis.opsForValue().setIfAbsent(lockKey, LoginUser.getId().toString());
    }

    private Boolean removeLock(String lockKey) {
        return redis.delete(lockKey);
    }

    @Resource
    public void setConverter(RetailConverter converter) {
        this.converter = converter;
    }

    @Autowired
    public Add(RetailRecordService recordService, RetailDrugRecordService drugRecordService, StockBatchService stockService, DataSourceTransactionManager transactionManager, TransactionDefinition transactionDefinition) {
        this.recordService = recordService;
        this.drugRecordService = drugRecordService;
        this.stockService = stockService;
        this.transactionManager = transactionManager;
        this.transactionDefinition = transactionDefinition;
    }
}
