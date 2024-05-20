package com.bbs.financial.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bbs.financial.entity.MoneyType;
import com.bbs.financial.service.MoneyTypeService;
import com.bbs.financial.mapper.MoneyTypeMapper;
import org.springframework.stereotype.Service;

/**
* @author 路晨霖
* @description 针对表【money_type(币别)】的数据库操作Service实现
* @createDate 2024-05-20 15:41:51
*/
@Service
public class MoneyTypeServiceImpl extends ServiceImpl<MoneyTypeMapper, MoneyType>
    implements MoneyTypeService{

}




