package com.bbs.financial.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bbs.financial.entity.AccountingSet;
import com.bbs.financial.mapper.AccountingSetMapper;
import com.bbs.financial.service.AccountingSetService;
import com.bbs.financial.util.LoginUser;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 */
@Service
public class AccountingSetServiceImpl extends ServiceImpl<AccountingSetMapper, AccountingSet>
    implements AccountingSetService{

    @Override
    public List<AccountingSet> getByLoginCompany() {
        return lambdaQuery().eq(AccountingSet::getCompanyId, LoginUser.getCompanyId()).list();
    }

    @Override
    public Boolean updateDelStatus(Long id) {
        return lambdaUpdate().eq(AccountingSet::getId, id).set(AccountingSet::getIsDeleted, 1).update();
    }
}




