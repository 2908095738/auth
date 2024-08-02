package com.clinic.controller;


import com.bbs.Result;
import com.clinic.cache.auxiliary.type.AuxiliaryTypeCache;
import com.clinic.entity.AuxiliaryType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.List;

@RestController
public class AuxiliaryTypeController {

    @Resource
    private AuxiliaryTypeCache cache;

    @GetMapping("/auxiliary/type")
    public Result<List<AuxiliaryType>> get() {
        try {
            return Result.success(cache.get());
        } catch (InterruptedException | ParseException e) {
            return Result.failed(e.getMessage());
        }
    }


    @PutMapping("/auxiliary/type")
    public Result<Boolean> set(@RequestParam("name")String name) {
        cache.set(name);
        return Result.success(true);
    }


}
