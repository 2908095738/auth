package com.bbs.financial.service;

import com.bbs.financial.entity.AccountingSet;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 *
 */
public interface AccountingSetService extends IService<AccountingSet> {

    List<AccountingSet> getByLoginCompany();

    Boolean updateDelStatus(Long id);
}
