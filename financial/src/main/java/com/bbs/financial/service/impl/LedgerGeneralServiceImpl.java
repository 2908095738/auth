package com.bbs.financial.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bbs.financial.entity.LedgerGeneral;
import com.bbs.financial.service.LedgerGeneralService;
import com.bbs.financial.mapper.LedgerGeneralMapper;
import org.springframework.stereotype.Service;

/**
* @author ludada
* @description 针对表【ledger_general(总账)】的数据库操作Service实现
* @createDate 2024-07-16 11:08:04
*/
@Service
public class LedgerGeneralServiceImpl extends ServiceImpl<LedgerGeneralMapper, LedgerGeneral>
    implements LedgerGeneralService{

}




