package com.clinic.app.retail;

import com.clinic.entity.RetailDrugRecord;
import com.clinic.entity.RetailRecord;
import com.clinic.entity.StockBatch;

import java.util.List;

public class Method {



    /**
     *
     * 计算售卖后的库存数量
     * @param retailDrugRecord RetailDrugRecord::number
     * @param stockBatch StockBatch::number
     * @return 售卖后的库存数量
     */
    public static Long computeSellAfterStockNumber(RetailDrugRecord retailDrugRecord, StockBatch stockBatch) {
        return stockBatch.getNumber() - retailDrugRecord.getNumber();
    }

    /**
     * 校验售卖后的库存数量是否正常
     * @param stockNumber 库存数量
     * @return 售卖后的库存数量是否正常
     */
    public static Boolean sellAfterStockNumberIsNormal(Long stockNumber) {
        return stockNumber >= 0;
    }

    /**
     * 填充 RetailId
     * @param retailDrugRecords RetailDrugRecords
     * @param retailRecord RetailRecord
     */
    public static void fillRetailIdToDrugRecords(List<RetailDrugRecord> retailDrugRecords, RetailRecord retailRecord) {
        retailDrugRecords.forEach(drug -> drug.setRetailId(retailRecord.getId()));
    }
}
