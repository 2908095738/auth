package com.clinic.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bbs.Result;
import com.clinic.dto.param.AddSettingsParam;
import com.clinic.dto.param.UpdateSettingsParam;
import com.clinic.entity.Settings;

import java.util.List;

/**
 *
 */
public interface SettingsService extends IService<Settings> {


    Result<Boolean> add(AddSettingsParam param);

    Result<Boolean> update(UpdateSettingsParam param);

    Settings getByUserId();

    Integer getUserSettingStockExpiryAlertMonth(Settings settings);

    List<String> getClinic(Long patientId);
}
