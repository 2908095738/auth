package com.clinic.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clinic.entity.AdmissionLog;
import com.clinic.entity.RetailDrugRecord;
import com.clinic.entity.RetailRecord;
import com.clinic.mapper.RetailRecordMapper;
import com.clinic.service.RetailRecordService;
import com.clinic.util.LoginUser;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ONE;

/**
* @author 路晨霖
* @description 针对表【retail_record(零售：消费记录)】的数据库操作Service实现
* @createDate 2023-11-21 02:29:30
*/
@Service
public class RetailRecordServiceImpl extends ServiceImpl<RetailRecordMapper, RetailRecord>
    implements RetailRecordService{

    @Override
    public Page<RetailRecord> list(Page<RetailRecord> page, String val, Long startDateLong, Long endDateLong) {
        MPJLambdaWrapper<RetailRecord> wrapper = new MPJLambdaWrapper<>();

        Date endDate = null;
        if(nonNull(endDateLong)) {
            // 如果两个时间相同，则判定为查询一天内的全部记录
            endDate = startDateLong.equals(endDateLong) ? new Date() : DateUtil.endOfDay(new Date(endDateLong));
        }

        Date finalEndDate = endDate;
        wrapper
                .selectAll(RetailRecord.class)
                .selectCollection("t1", RetailDrugRecord.class, RetailRecord::getRetailDrugRecords)
                .leftJoin(RetailDrugRecord.class, RetailDrugRecord::getRetailId, RetailRecord::getId)

                .eq(RetailRecord::getUserId, LoginUser.getId())
                .and(StringUtils.isNotBlank(val), ext -> ext
                        .like(RetailRecord::getName, val)
                        .or(NumberUtil.isNumber(val), ext2 -> ext2
                                .likeRight(RetailRecord::getPhone, val)
                        )
                )
                .and(nonNull(startDateLong) && nonNull(endDate), ext -> ext
                        .ge(AdmissionLog::getCreateTime, new Date(startDateLong))
                        .lt(AdmissionLog::getCreateTime, finalEndDate)
                )
                .orderByDesc(RetailRecord::getCreateTime)
        ;
        return baseMapper.selectJoinPage(page, RetailRecord.class, wrapper);
    }
}




