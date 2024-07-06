package com.clinic.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.clinic.dto.param.DrugParam;
import com.clinic.entity.Drug;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 *
 */
public interface DrugService extends IService<Drug> {

    List<Drug> selectInfoDistinct();

    Page<Drug> search(DrugParam param);
}
