package com.bbs.financial.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bbs.financial.entity.FinanceCategory;
import com.bbs.financial.service.FinanceCategoryService;
import com.bbs.financial.mapper.FinanceCategoryMapper;
import org.springframework.stereotype.Service;

/**
* @author Mafty
* @description 针对表【finance_category(资产类别)】的数据库操作Service实现
* @createDate 2024-05-14 15:10:43
*/
@Service
public class FinanceCategoryServiceImpl extends ServiceImpl<FinanceCategoryMapper, FinanceCategory>
    implements FinanceCategoryService{

}




