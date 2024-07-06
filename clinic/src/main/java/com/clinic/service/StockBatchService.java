package com.clinic.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.clinic.dto.param.AddRetailParams;
import com.clinic.entity.StockBatch;

import java.math.BigDecimal;
import java.util.List;

/**
* @author 路晨霖
* @description 针对表【stock_batch】的数据库操作Service
* @createDate 2023-09-27 12:44:37
*/
public interface StockBatchService extends IService<StockBatch> {

    List<StockBatch> searchByApprovalNumbers(List<String> approvalNumbers, List<String> batchNumbers);

    StockBatch searchByApprovalNumber(String approvalNumber);

    StockBatch getByApprovalNumberAndBatchNumber(String approvalNumber, String batchNumber);

    StockBatch searchBatchAndUnitByID(Long id);

    Boolean update(List<StockBatch> stockBatches);

    List<StockBatch> searchWaitUpdateStocks(AddRetailParams params);

    Boolean updateCost(Long id, BigDecimal cost, Integer unitId);

    /**
     * 库存批次药品是否即将过期
     * @param stockBatch 库存批次药品
     * @return 是否即将过期
     */
    Boolean stockIsExpiry(StockBatch stockBatch);
}
