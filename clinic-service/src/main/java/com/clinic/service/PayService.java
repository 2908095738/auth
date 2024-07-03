package com.clinic.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.clinic.dto.GetPayDto;
import com.clinic.dto.PayAndRecordPageDto;
import com.clinic.dto.param.GetPayParam;
import com.clinic.entity.Pay;


/**
 * 收费记录
 */
public interface PayService extends IService<Pay> {

    Page<PayAndRecordPageDto> selectPayAndRecordPageDto(GetPayParam param);

    GetPayDto getPay(Long id);
}
