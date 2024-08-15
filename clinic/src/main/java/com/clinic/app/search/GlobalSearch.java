package com.clinic.app.search;

import com.bbs.Result;
import com.clinic.entity.Drug;
import com.clinic.entity.DrugDetail;
import com.clinic.entity.Patient;
import com.clinic.service.DrugDetailService;
import com.clinic.service.DrugService;
import com.clinic.service.PatientService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@RequestMapping
@RestController
public class GlobalSearch {

    @Resource
    private PatientService patientService;

    @Resource
    private DrugService drugService;

    @Resource
    private DrugDetailService drugDetailService;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class VO {

        private List<Patient> patients;

        private List<Drug> drugs;

        private List<DrugDetail> drugDetails;

    }

    @GetMapping("/search")
    public Result<VO> search(@RequestParam String val) {
        List<Patient> patients = new ArrayList<>();
        List<Drug> drugs = new ArrayList<>();
        List<DrugDetail> drugDetails = new ArrayList<>();
        if(StringUtils.isNotBlank(val)) {
            patients = patientService.select(val);          //搜病人
            drugs = drugService.search(val);                  //搜药品
            drugDetails = drugDetailService.search(val);    //搜库存
        }
        return Result.success(new VO(patients, drugs, drugDetails));
    }
}
