package com.clinic.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clinic.dto.param.RecordAdmissionLogParam;
import com.clinic.dto.param.SearchAdmissionParam;
import com.clinic.dto.param.StatsParam;
import com.clinic.entity.AdmissionLog;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.yulichang.base.MPJBaseService;

import java.text.ParseException;

/**
* @author 路晨霖
* @description 针对表【admission_log(接诊日志)】的数据库操作Service
* @createDate 2023-11-03 18:41:33
*/
public interface AdmissionLogService extends MPJBaseService<AdmissionLog> {

    Page<AdmissionLog> search(SearchAdmissionParam param) throws ParseException;

    Long save(RecordAdmissionLogParam param);

    Boolean update(Long admissionId, Long id, Long payId);

    Long countPatientNum(StatsParam param);

    boolean updateEndState(Long admissionId);
}
