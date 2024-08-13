package com.bbs.financial.converter;

import cn.hutool.core.date.DateTime;
import com.bbs.financial.api.invoice.add.AddAuxByInovice;
import com.bbs.financial.api.invoice.add.AddCertByInvoice;
import com.bbs.financial.api.invoice.add.AddInvoice;
import com.bbs.financial.api.invoice.update.UpdateInvoice;
import com.bbs.financial.api.invoice.update.UpdateTaxBurden;
import com.bbs.financial.dto.ExcelInvoiceDto;
import com.bbs.financial.dto.InvoiceDto;
import com.bbs.financial.dto.TaxBurdenDto;
import com.bbs.financial.entity.AccountAuxiliary;
import com.bbs.financial.entity.Invoice;
import com.bbs.financial.entity.InvoiceDetail;
import com.bbs.financial.entity.TaxBurdenCal;
import com.bbs.financial.enums.*;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@Mapper(componentModel = "spring")
public interface InvoiceConverter {
    @Mapping(source = "invoiceStatus", target = "invoiceStatus", qualifiedByName = "resetInvStatus")
    @Mapping(source = "invoiceType", target = "invoiceType", qualifiedByName = "resetInvType")
    @Mapping(source = "invoiceCategory", target = "invoiceCategory", qualifiedByName = "resetInvCate")
    @Mapping(source = "taxType", target = "taxType", qualifiedByName = "resetTaxType")

    @Mapping(target = "isInvoiceDetail", ignore = true)
    @Mapping(target = "details", ignore = true)
    Invoice toEntity(AddInvoice.Param param);

    @Mapping(source = "invoiceStatus", target = "invoiceStatus", qualifiedByName = "resetInvStatus")
    @Mapping(source = "invoiceType", target = "invoiceType", qualifiedByName = "resetInvType")
    @Mapping(source = "invoiceCategory", target = "invoiceCategory", qualifiedByName = "resetInvCate")
    @Mapping(source = "taxType", target = "taxType", qualifiedByName = "resetTaxType")

    @Mapping(target = "isInvoiceDetail", ignore = true)
    @Mapping(target = "details", ignore = true)
    Invoice toEntity(UpdateInvoice.Param param);

    InvoiceDetail toEntity(UpdateInvoice.DetailParam param);

    AccountAuxiliary toEntity(AddAuxByInovice.Param param);

    @Mapping(source = "type", target = "type", qualifiedByName = "resetCalType")
    @Mapping(source = "money", target = "money", qualifiedByName = "resetMoney")
    @Mapping(source = "taxMoney", target = "taxMoney", qualifiedByName = "resetMoney")
    TaxBurdenCal toEntity(UpdateTaxBurden.Item param);

    @Mapping(target = "details", ignore = true)
    Invoice toEntity(AddCertByInvoice.Param param);

    @Mapping(source = "invoiceStatus", target = "invoiceStatus", qualifiedByName = "getInvStatus")
    @Mapping(source = "invoiceType", target = "invoiceType", qualifiedByName = "getInvType")
    @Mapping(source = "taxType", target = "taxType", qualifiedByName = "getTaxType")
    InvoiceDto toDto(Invoice invoice);

    @Mapping(source = "openDate", target = "openDateStr", qualifiedByName = "getDate")
    @Mapping(source = "invoiceType", target = "invoiceTypeStr", qualifiedByName = "getInvTypeStr")
    @Mapping(source = "details", target = "nonTaxMoneyStr", qualifiedByName = "getNonTaxStr")
    @Mapping(source = "details", target = "taxMoneyStr", qualifiedByName = "getTaxStr")
    @Mapping(source = "details", target = "taxTotalStr", qualifiedByName = "getTotalStr")
    @Mapping(source = "invoiceStatus", target = "invoiceStatusStr", qualifiedByName = "getInvStatusStr")
    @Mapping(source = "createTime", target = "createTimeStr", qualifiedByName = "getDate")
    ExcelInvoiceDto toDto(InvoiceDto invoice);

    @Mapping(source = "type", target = "type", qualifiedByName = "getTaxBurdenCalType")
    TaxBurdenDto.TaxBurdenBase toDto(TaxBurdenCal entity);

    @Named("resetInvStatus")
    default InvStatusEnum toInvStatus(Integer invoiceStatus) {
        return InvStatusEnum.enumMap.get(invoiceStatus);
    }

    @Named("resetInvType")
    default InvTypeEnum toInvType(Integer invoiceType) {
        return InvTypeEnum.enumMap.get(invoiceType);
    }

    @Named("resetInvCate")
    default InvCateEnum toInvCate(Integer invoiceCategory) {
        return InvCateEnum.enumMap.get(invoiceCategory);
    }

    @Named("resetTaxType")
    default TaxTypeEnum toTaxType(Integer taxType) {
        return TaxTypeEnum.enumMap.get(taxType);
    }

    @Named("resetCalType")
    default TaxBurdenCalTypeEnum toCalType(Integer calType) {
        return TaxBurdenCalTypeEnum.enumMap.get(calType);
    }

    @Named("resetMoney")
    default BigDecimal toMoneyByStr(String moneyStr) {
        if (StringUtils.isNotBlank(moneyStr))
            return new BigDecimal(moneyStr);
        return null;
    }

    @Named("getInvStatus")
    default Integer toInvStatus(InvStatusEnum invStatusEnum) {
        return invStatusEnum.getCode();
    }

    @Named("getInvType")
    default Integer toInvType(InvTypeEnum invTypeEnum) {
        return invTypeEnum.getCode();
    }

    @Named("getTaxType")
    default Integer toTaxType(TaxTypeEnum taxType) {
        if (Objects.nonNull(taxType)) return taxType.getCode();
        return null;
    }

    @Named(("getDate"))
    default String toStr(Date date) {
        return DateTime.of(date).toDateStr();
    }

    @Named(("getInvTypeStr"))
    default String toStrByInvType(Integer invoiceType) {
        return InvTypeEnum.enumMap.get(invoiceType).getMsg();
    }

    @Named(("getInvStatusStr"))
    default String toStrByInvStatus(Integer invoiceStatus) {
        return InvStatusEnum.enumMap.get(invoiceStatus).getMsg();
    }

    @Named(("getNonTaxStr"))
    default String toNonTax(List<InvoiceDetail> details) {
        BigDecimal money = BigDecimal.ZERO;
        for (InvoiceDetail detail : details)
            money = money.add(getMoney(detail::getNonTaxMoney));
        return money.toString();
    }

    default BigDecimal getMoney(Supplier<BigDecimal> getMoneyFunc) {
        if (!ObjectUtils.isEmpty(getMoneyFunc.get())) return getMoneyFunc.get();
        return BigDecimal.ZERO;
    }

    @Named(("getTaxStr"))
    default String toTax(List<InvoiceDetail> details) {
        BigDecimal money = BigDecimal.ZERO;
        for (InvoiceDetail detail : details)
            money = money.add(getMoney(detail::getTaxMoney));
        return money.toString();
    }

    @Named(("getTotalStr"))
    default String toTotal(List<InvoiceDetail> details) {
        BigDecimal money = BigDecimal.ZERO;
        for (InvoiceDetail detail : details) {
            money = money.add(getMoney(detail::getNonTaxMoney));
            money = money.add(getMoney(detail::getTaxMoney));
        }
        return money.toString();
    }

    @Named("getTaxBurdenCalType")
    default Integer toTaxBurdenCalType(TaxBurdenCalTypeEnum typeEnum) {
        if (Objects.nonNull(typeEnum)) return typeEnum.getCode();
        return null;
    }
}