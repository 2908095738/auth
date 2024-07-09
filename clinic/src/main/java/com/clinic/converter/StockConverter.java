package com.clinic.converter;

import com.clinic.dto.param.PutStockParam;
import com.clinic.dto.vo.PrescriptionSearchDrugVO;
import com.clinic.entity.Stock;
import com.clinic.entity.StockBatch;
import com.clinic.entity.StockInDrug;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StockConverter {

    Stock toEntity(PutStockParam param);

    @Mapping(source = "type", target = "dosageForm")
    @Mapping(source = "countNumber", target = "countVal")
    @Mapping(source = "countUnitId", target = "countUnitId")
    @Mapping(source = "usage", target = "drugUsage")
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "stateCountRule", ignore = true)
    StockBatch toBatchEntity(PutStockParam param);

    @Mapping(source = "type", target = "dosageForm")
    StockInDrug toStockInDrugEntity(PutStockParam param);

    @Mapping(target = "singleDoseUnit", ignore = true)
    @Mapping(target = "expiryDate", ignore = true)
    @Mapping(source = "number", target = "stockNumber")
    PrescriptionSearchDrugVO toPrescriptionSearchDrugVO(StockBatch stockBatch);
}
