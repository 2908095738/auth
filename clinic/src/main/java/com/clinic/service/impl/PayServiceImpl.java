package com.clinic.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clinic.dto.GetPayDto;
import com.clinic.dto.PayAndRecordPageDto;
import com.clinic.dto.param.GetPayParam;
import com.clinic.entity.AdmissionLog;
import com.clinic.entity.Patient;
import com.clinic.entity.Pay;
import com.clinic.entity.PayRecord;
import com.clinic.mapper.PayMapper;
import com.clinic.service.PayService;
import com.clinic.util.LoginUser;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ONE;

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
                .eq(nonNull(param.getState()), Pay::getState, param.getState())
                .and(nonNull(param.getStartDate()) && nonNull(param.getEndDate()), ext -> ext
                        .ge(Patient::getCreateTime, param.getStartDate())
                        .lt(Patient::getCreateTime, param.getEndDate())
                )
                .eq(nonNull(param.getName()), Patient::getName, param.getName())
                .eq(nonNull(param.getPhone()), Patient::getPhone, param.getPhone())
                .eq(Pay::getCreator, uid
                ));
    }

    @Override
    public List<PayAndRecordPageDto> selectPayAndRecordDto(GetPayParam param) {
        Long uid = LoginUser.getId();
        return baseMapper.selectJoinList(PayAndRecordPageDto.class, new MPJLambdaWrapper<Pay>()
                .select(Patient::getName, Patient::getSex, Patient::getAge, Patient::getAddress, Patient::getPhone)
                .select(Pay::getDossierTime, Pay::getFee)
                .selectCollection(PayRecord.class, PayAndRecordPageDto::getPayRecordList)
                .leftJoin(Patient.class, Patient::getId, Pay::getPatientId)
                .leftJoin(PayRecord.class, PayRecord::getPayId, Pay::getId)
                .eq(nonNull(param.getState()), Pay::getState, param.getState())
                .and(nonNull(param.getStartDate()) && nonNull(param.getEndDate()), ext -> ext
                        .ge(Patient::getCreateTime, param.getStartDate())
                        .lt(Patient::getCreateTime, param.getEndDate())
                )
                .and(nonNull(param.getName()) || nonNull(param.getPhone()) || nonNull(param.getAddress()), ext -> ext
                        .like(nonNull(param.getName()), Patient::getName, param.getName())
                        .or()
                        .like(nonNull(param.getPhone()), Patient::getPhone, param.getPhone())
                        .or()
                        .like(nonNull(param.getAddress()), Patient::getAddress,param.getAddress())
                )
                .eq(Pay::getCreator, uid)
        );
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




