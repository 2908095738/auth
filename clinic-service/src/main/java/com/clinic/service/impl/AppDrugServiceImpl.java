package com.clinic.service.impl;

import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import cn.hutool.core.lang.tree.Tree;
import com.bbs.Result;
import com.clinic.dto.Manufacturer;
import com.clinic.dto.param.DrugParam;
import com.clinic.entity.Drug;
import com.clinic.entity.Unit;
import com.clinic.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

import static com.bbs.Result.failed;
import static com.bbs.Result.success;
import static java.util.Objects.isNull;

@Service
@Slf4j
public class AppDrugServiceImpl implements AppDrugService {

    @Autowired
    private DrugService drugService;

    @Autowired
    private ManufacturerService manufacturerService;

    @Autowired
    private UnitCascadeService unitCascadeService;

    @Autowired
    private UnitService unitService;

    @Resource(name = "excelBatchImport")
    private ThreadPoolExecutor threadPoolExecutor;

    @Override
    public Result search(DrugParam param) {
        return success(drugService.search(param));
    }

    @Override
    public Result excelImport(MultipartFile file) {
        if(isNull(file)){
            return failed(1003,false,"请选择文件！");
        }
        try {
            ImportParams params = new ImportParams();
            params.setTitleRows(1);
            params.setHeadRows(2);
            List<Drug> allDrugList = ExcelImportUtil.importExcel(file.getInputStream(), Drug.class, params);
            CompletableFuture.allOf(ListUtils.partition(allDrugList, 5000).stream()
                    .map(drugList -> CompletableFuture.supplyAsync(() -> drugService.saveBatch(drugList), threadPoolExecutor))
                    .toArray(CompletableFuture[]::new)
            ).get();
            return success(true);
        } catch (Exception e) {
            return failed(1004,false,e.getMessage());
        }
    }

    @Override
    public Result searchManufacturer(Manufacturer param) {
        return Result.success(manufacturerService.search(param));
    }

    @Override
    public Result<List<Unit>> searchAllDefUnit() {
        return success(unitService.searchAllDefUnit());
    }

    @Override
    public Result<List<Unit>> searchNoRepeatAllDefUnit() {
        return success(unitService.searchNoRepeatAllDefUnit());
    }

    @Override
    public Result<List<Tree<Integer>>> getDefUnitTrace() {
        return success(unitService.getTraceDefUnit());
    }
}
