package com.clinic.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clinic.dto.GetPayDto;
import com.clinic.dto.PayAndRecordPageDto;
import com.clinic.dto.param.GetPayParam;
import com.clinic.entity.Patient;
import com.clinic.entity.Pay;
import com.clinic.entity.PayRecord;
import com.clinic.mapper.PayMapper;
import com.clinic.service.PayService;
import com.clinic.util.LoginUser;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 *
 */
@Service
public class PayServiceImpl extends ServiceImpl<PayMapper, Pay>
    implements PayService {

    @Override
    public Page<PayAndRecordPageDto> selectPayAndRecordPageDto(GetPayParam param) {
        Long uid = LoginUser.getId();
        return baseMapper.selectJoinPage(param.toPage(), PayAndRecordPageDto.class, new MPJLambdaWrapper<Pay>()
                .select(Patient::getName, Patient::getSex, Patient::getAge, Patient::getAddress, Patient::getPhone)
                .select(Pay::getDossierTime, Pay::getFee)
                .selectCollection(PayRecord.class, PayAndRecordPageDto::getPayRecordList)
                .leftJoin(Patient.class, Patient::getId, Pay::getPatientId)
                .leftJoin(PayRecord.class, PayRecord::getPayId, Pay::getId)
                .eq(Objects.nonNull(param.getName()), Patient::getName, param.getName())
                .eq(Objects.nonNull(param.getPhone()), Patient::getPhone, param.getPhone())
                .eq(Objects.nonNull(param.getState()), Pay::getState, param.getState())
                .eq(Pay::getCreator, uid
                ));
    }

    @Override
    public GetPayDto getPay(Long id) {
        return baseMapper.selectJoinOne(GetPayDto.class, new MPJLambdaWrapper<Pay>()
                .selectAll(Pay.class)
                .selectCollection(PayRecord.class, GetPayDto::getPayRecordDtos)
                .leftJoin(PayRecord.class, PayRecord::getPayId, Pay::getId)
                .eq(Pay::getId, id)
        );
    }
}




