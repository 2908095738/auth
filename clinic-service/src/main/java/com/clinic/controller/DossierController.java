package com.clinic.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.clinic.dto.param.SaveDossierParam;
import com.clinic.dto.param.UpdateDossierParam;
import com.clinic.entity.Dossier;
import com.clinic.service.DossierService;
import com.clinic.util.LoginUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 病历
 */
@RestController
@RequestMapping("/dossier")
public class DossierController {
    @Resource
    private DossierService service;

    /**
     * 病例添加
     * @param dossier 病例信息
     * @return null
     */
    @PutMapping
    public Result<Long> addDossier(@RequestBody @Valid SaveDossierParam dossier){
        return service.createDossier(dossier);
    }

    /**
     * 病例修改
     * @param dossier 病例信息
     * @return null
     */
    @PostMapping
    public Result<Long> updateDossier(@RequestBody @Valid UpdateDossierParam dossier){
        return service.updateDossier(dossier);
    }


    /**
     * 病例查询
     * @param id 病人ID
     * @param current 页码
     * @param size 条数
     * @return null
     */
    @GetMapping
    public Result<Page<Dossier>> selectDossier(String id, Integer current, Integer size){
        return service.select(LoginUser.getId(), id, current, size);
    }
}