package com.clinic.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clinic.dto.param.SearchRetailRecordParam;
import com.clinic.entity.RetailDrugRecord;
import com.clinic.entity.RetailRecord;
import com.clinic.mapper.RetailRecordMapper;
import com.clinic.service.RetailRecordService;
import com.clinic.util.LoginUser;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.stereotype.Service;

/**
* @author 路晨霖
* @description 针对表【retail_record(零售：消费记录)】的数据库操作Service实现
* @createDate 2023-11-21 02:29:30
*/
@Service
public class RetailRecordServiceImpl extends ServiceImpl<RetailRecordMapper, RetailRecord>
    implements RetailRecordService{

    @Override
    public Page<RetailRecord> list(SearchRetailRecordParam param) {
        MPJLambdaWrapper<RetailRecord> wrapper = new MPJLambdaWrapper<>();

        wrapper
                .selectAll(RetailRecord.class)
                .selectCollection("t1", RetailDrugRecord.class, RetailRecord::getRetailDrugRecords)
                .leftJoin(RetailDrugRecord.class, RetailDrugRecord::getRetailId, RetailRecord::getId)

                .eq(RetailRecord::getUserId, LoginUser.getId())
                .orderByDesc(RetailRecord::getCreateTime)
        ;
        return baseMapper.selectJoinPage(param.toPage(), RetailRecord.class, wrapper);
    }
}




