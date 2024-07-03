package com.clinic.cache.prescription;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.clinic.dto.vo.PrescriptionSearchDrugVO;


@FunctionalInterface
public interface SearchPrescriptionDrug {

    Result<Page<PrescriptionSearchDrugVO>> search(String drugName) throws InterruptedException;
}
