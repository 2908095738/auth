package com.bbs.financial.service.impl;

import com.bbs.financial.entity.AccountRemark;
import com.bbs.financial.service.AccountRemarkService;
import com.bbs.financial.mapper.AccountRemarkMapper;
import com.github.yulichang.base.MPJBaseServiceImpl;
import org.springframework.stereotype.Service;

/**
* @author 路晨霖
* @description 针对表【account_remark(科目备注（用于记账凭证 -> 摘要）)】的数据库操作Service实现
* @createDate 2024-05-10 23:34:18
*/
@Service
public class AccountRemarkServiceImpl extends MPJBaseServiceImpl<AccountRemarkMapper, AccountRemark>
    implements AccountRemarkService{

}




