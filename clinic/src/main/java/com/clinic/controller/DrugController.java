package com.clinic.controller;


import com.bbs.Result;
import com.clinic.dto.Manufacturer;
import com.clinic.dto.param.DrugParam;
import com.clinic.service.AppDrugService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping
public class DrugController {

    private AppDrugService appDrugService;

    @GetMapping("/drug")
    public Result search(DrugParam param) {
        return appDrugService.search(param);
    }

    @GetMapping("/manufacturer")
    public Result searchManufacturer(Manufacturer param) {
        return appDrugService.searchManufacturer(param);
    }

    /**
     * 批量入库药品信息
     */
    @PostMapping("/import/excel")
    public Result excelImport(@RequestParam("file") MultipartFile file){
        return appDrugService.excelImport(file);
    }

    @GetMapping("/drug/unit/list/all")
    public Result getAllUnitList() {
        return appDrugService.searchAllDefUnit();
    }

    @GetMapping("/drug/unit/list")
    public Result getAllNoRepeatUnitList() {
        return appDrugService.searchNoRepeatAllDefUnit();
    }

    @GetMapping("/drug/unit/trace")
    public Result getUnitTrace() {
        return appDrugService.getDefUnitTrace();
    }

    @Autowired
    public void setAppDrugService(AppDrugService appDrugService) {
        this.appDrugService = appDrugService;
    }
}
