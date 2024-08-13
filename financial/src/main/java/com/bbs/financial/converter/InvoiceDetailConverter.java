package com.bbs.financial.converter;

import com.bbs.financial.api.invoice.add.AddInvoice;
import com.bbs.financial.entity.InvoiceDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InvoiceDetailConverter {
    @Mapping(target = "price", ignore = true)
    InvoiceDetail toEntity(AddInvoice.DetailParam param);
}