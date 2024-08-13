package com.bbs.financial.service.impl;

import com.bbs.financial.entity.InvoiceDetail;
import com.bbs.financial.mapper.InvoiceDetailMapper;
import com.bbs.financial.service.InvoiceDetailService;
import com.github.yulichang.base.MPJBaseServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @author Mafty
 * @description 针对表【invoice_detail(发票明细)】的数据库操作Service实现
 * @createDate 2024-06-20 14:29:56
 */
@Service
public class InvoiceDetailServiceImpl extends MPJBaseServiceImpl<InvoiceDetailMapper, InvoiceDetail>
        implements InvoiceDetailService {
}