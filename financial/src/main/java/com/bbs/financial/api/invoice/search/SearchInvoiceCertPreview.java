package com.bbs.financial.api.invoice.search;

import cn.hutool.core.collection.CollectionUtil;
import com.bbs.Result;
import com.bbs.financial.dto.InvoiceCertDto;
import com.bbs.financial.dto.InvoiceDto;
import com.bbs.financial.entity.*;
import com.bbs.financial.service.AccountAuxiliaryService;
import com.bbs.financial.service.AccountAuxiliaryTypeService;
import com.bbs.financial.util.SpringUtil;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/invoice")
public class SearchInvoiceCertPreview {

    @Resource
    private ApplicationContext applicationContext;

    @Resource
    private AccountAuxiliaryService subjAuxService;

    @Resource
    private AccountAuxiliaryTypeService typeService;

    private class StringTip {
        public static final String NO_TEMP = "请到发票模板管理设置默认模板";

        public static final String SUBJ_DIRE_D = "贷";

        public static final String SUBJ_DIRE_J = "借";

        public static final String DAI_AUX_ERR = "匹配失败，请重新选择";

        public static final String JIE_AUX_ERR = "请选择辅助核算";

        public static final String FORMAT_AUX = "%s -> %s";
    }

    /**
     * 获取发票生成凭证预览列表
     *
     * @param idsStr       伪发票id数组字符串
     * @param invoiceCate  发票分类：0.销项发票;1.进项发票;2.费用小票;
     * @param isShowDetail 是否显示发票明细/
     */
    @ResponseBody
    @GetMapping("/cert/preview")
    public Result<List<InvoiceCertDto>> search(@RequestParam String idsStr, @RequestParam Integer invoiceCate, @RequestParam Boolean isShowDetail) {
        List<InvoiceDto> invoiceList = getInvoiceList(idsStr, invoiceCate, isShowDetail);

        //获取凭证模板列表
        SearchTemplate searchTemplate = (SearchTemplate) applicationContext.getBean(SpringUtil.getClassNameOfFirstLow(SearchTemplate.class));
        CertificateTemplate template = searchTemplate.getDefault(invoiceCate).getData();

        if (Objects.isNull(template))
            return Result.failed(StringTip.NO_TEMP);

        //获取发票明细使用的模板摘要
        CertificateTemplateAbstract detailByTemp = getAbstByDetail(template);

        //辅助核算id和辅助核算的映射
        Set<String> auxTypeIdSet = getAuxTypeIdList(template);
        Map<Long, AccountAuxiliary> objByIdOfAux = getSubjAuxMap(auxTypeIdSet);

        //辅助核算id和辅助核算使用次数的映射
        Map<String, Integer> useNumByAuxIdOfAux = auxTypeIdSet.stream().collect(Collectors.toMap(String::toString, i -> NumberUtils.INTEGER_ZERO));

        //辅助核算类型id和辅助核算类型名称的映射
        Map<Long, String> nameByIdOfType = getTypeMap(auxTypeIdSet);

        List<InvoiceCertDto> resultList = new ArrayList<>();
        for (InvoiceDto dto : invoiceList) {
            //贷方价税/不含税金额摘要
            List<InvoiceCertDto.InvoiceCertAbst> detailList = new ArrayList<>();
            initAbstList(detailByTemp, dto.getDetails(), detailList, objByIdOfAux, useNumByAuxIdOfAux, nameByIdOfType);

            //贷方税额摘要
            boolean isTaxByDai = detailByTemp.getMoneyType().equals(NumberUtils.INTEGER_TWO) && !detailList.isEmpty();
            InvoiceCertDto.InvoiceCertAbst taxAbst = getTaxAbst(isTaxByDai, template, dto.getDetails());

            //借方摘要
            InvoiceCertDto.InvoiceCertAbst jieAbst = getJieAbst(template, isTaxByDai, detailList, taxAbst);

            //发票生成凭证预览赋值
            {
                InvoiceCertDto certDto = new InvoiceCertDto();
                certDto.setInvoice(dto);
                certDto.setTempName(template.getName());

                certDto.setAuxResetMapByType(getDoneResetAuxIdList(useNumByAuxIdOfAux, nameByIdOfType));
                certDto.setAbsts(getAbstListByCert(template.getTemplateAbstractList(), jieAbst, taxAbst, detailList));

                resultList.add(certDto);
            }
        }

        return Result.success(resultList);
    }

    /**
     * 获取发票列表
     *
     * @param idsStr       伪发票id数组字符串
     * @param invoiceCate  发票分类：0.销项发票;1.进项发票;2.费用小票;
     * @param isShowDetail 是否显示发票明细
     */
    private List<InvoiceDto> getInvoiceList(String idsStr, Integer invoiceCate, Boolean isShowDetail) {
        List<Long> idList = SpringUtil.str2ListByQs(idsStr, Long::valueOf);
        SearchInvoice searchInvoice = (SearchInvoice) applicationContext.getBean(SpringUtil.getClassNameOfFirstLow(SearchInvoice.class));
        return searchInvoice.search(invoiceCate, isShowDetail, idList);
    }

    /**
     * 获取发票明细使用的模板摘要
     *
     * @param template 凭证模板
     */
    private CertificateTemplateAbstract getAbstByDetail(CertificateTemplate template) {
        return template.getTemplateAbstractList().stream()
                .filter(a -> a.getAccount().getDirection().equals(StringTip.SUBJ_DIRE_D))
                .filter(a -> a.getMoneyType() != NumberUtils.INTEGER_ONE)
                .findFirst()
                .get();
    }

    /**
     * 获取辅助核算类型id列表
     *
     * @param template 凭证模板
     */
    private Set<String> getAuxTypeIdList(CertificateTemplate template) {
        List<Account> auxTypeIdList = template.getTemplateAbstractList().stream()
                .map(CertificateTemplateAbstract::getAccount)
                .filter(a -> StringUtils.isNotBlank(a.getAccountAuxiliaryTypeIds()))
                .collect(Collectors.toList());

        List<String> tmpAuxTypeIdList = auxTypeIdList.stream().map(Account::getAccountAuxiliaryTypeIds).collect(Collectors.toList());
        return CollectionUtil.newHashSet(tmpAuxTypeIdList);
    }

    /**
     * 获取辅助核算映射
     *
     * @param auxTypeIdSet 辅助核算类型id集合
     * @return key: AccountAuxiliary.id
     */
    private Map<Long, AccountAuxiliary> getSubjAuxMap(Set<String> auxTypeIdSet) {
        if (auxTypeIdSet.isEmpty())
            return Collections.emptyMap();

        List<AccountAuxiliary> auxList = subjAuxService.selectJoinList(AccountAuxiliary.class,
                new MPJLambdaWrapper<AccountAuxiliary>()
                        .eq(AccountAuxiliary::getIsDeleted, NumberUtils.INTEGER_ZERO)
                        .in(AccountAuxiliary::getTypeId, auxTypeIdSet)
        );

        return auxList.stream().collect(Collectors.toMap(AccountAuxiliary::getId, a -> a));
    }

    /**
     * 获取辅助核算类型映射
     *
     * @param auxTypeIdSet 辅助核算类型id集合
     * @return key: 辅助核算类型id; value:辅助核算类型名称
     */
    private Map<Long, String> getTypeMap(Set<String> auxTypeIdSet) {
        if (auxTypeIdSet.isEmpty())
            return Collections.emptyMap();

        List<AccountAuxiliaryType> typeList = typeService.selectJoinList(AccountAuxiliaryType.class, new MPJLambdaWrapper<AccountAuxiliaryType>()
                .select(AccountAuxiliaryType::getId, AccountAuxiliaryType::getName)
                .in(AccountAuxiliaryType::getId, auxTypeIdSet)
        );

        return typeList.stream().collect(Collectors.toMap(AccountAuxiliaryType::getId, AccountAuxiliaryType::getName));
    }

    /**
     * 初始化发票生成凭证预览
     *
     * @param detailByTemp       凭证模板摘要
     * @param detailList         发票明细列表
     * @param doneList           处理完成列表
     * @param objByIdOfAux       辅助核算id和辅助核算的映射
     * @param useNumByAuxIdOfAux 辅助核算类型id和辅助核算类型名称的映射
     * @param nameByIdOfType     辅助核算类型id和辅助核算类型名称的映射
     */
    private void initAbstList(CertificateTemplateAbstract detailByTemp, List<InvoiceDetail> detailList, List<InvoiceCertDto.InvoiceCertAbst> doneList, Map<Long, AccountAuxiliary> objByIdOfAux, Map<String, Integer> useNumByAuxIdOfAux, Map<Long, String> nameByIdOfType) {
        switch (detailByTemp.getMoneyType()) {
            case 0://价税合计
                for (InvoiceDetail detail : detailList)
                    doneList.add(getAbst(detailByTemp, detail, true, objByIdOfAux, useNumByAuxIdOfAux, nameByIdOfType));
                break;
            case 2://不含税金额
                for (InvoiceDetail detail : detailList)
                    doneList.add(getAbst(detailByTemp, detail, false, objByIdOfAux, useNumByAuxIdOfAux, nameByIdOfType));
                break;
        }
    }

    /**
     * 获取凭证摘要
     *
     * @param detailByTemp       凭证模板摘要
     * @param detail             发票明细
     * @param isTax              true: 价税合计; false: 不含税金额
     * @param objByIdOfAux       辅助核算id和辅助核算的映射
     * @param useNumByAuxIdOfAux 辅助核算类型id和辅助核算类型名称的映射
     * @param nameByIdOfType     辅助核算类型id和辅助核算类型名称的映射
     */
    private InvoiceCertDto.InvoiceCertAbst getAbst(CertificateTemplateAbstract detailByTemp, InvoiceDetail detail, boolean isTax, Map<Long, AccountAuxiliary> objByIdOfAux, Map<String, Integer> useNumByAuxIdOfAux, Map<Long, String> nameByIdOfType) {
        InvoiceCertDto.InvoiceCertAbst doneAbst = new InvoiceCertDto.InvoiceCertAbst();
        doneAbst.setId(detail.getId());
        doneAbst.setCertificateAbstract(detailByTemp.getCertificateAbstract());
        doneAbst.setNo(detailByTemp.getAccount().getNo());
        doneAbst.setName(detailByTemp.getAccount().getName());
        doneAbst.setMoneyType(detailByTemp.getMoneyType());

        //凭证模板-凭证摘要-科目有辅助核算
        if (!objByIdOfAux.isEmpty()) {
            //辅助核算赋值
            AccountAuxiliary aux = objByIdOfAux.get(detail.getAbstAuxId());
            if (Objects.isNull(aux)) {
                doneAbst.setAbstAuxId(NumberUtils.LONG_MINUS_ONE);
                doneAbst.setAuxiliary(StringTip.DAI_AUX_ERR);

                useNumByAuxIdOfAux.computeIfPresent(detailByTemp.getAccount().getAccountAuxiliaryTypeIds(), (k, v) -> v - NumberUtils.INTEGER_ONE);
            } else {
                String auxTypeName = nameByIdOfType.get(Long.valueOf(detailByTemp.getAccount().getAccountAuxiliaryTypeIds()));
                doneAbst.setAbstAuxId(detail.getAbstAuxId());
                doneAbst.setAuxiliary(String.format(StringTip.FORMAT_AUX, auxTypeName, aux.getName()));

                useNumByAuxIdOfAux.computeIfPresent(detailByTemp.getAccount().getAccountAuxiliaryTypeIds(), (k, v) -> v + NumberUtils.INTEGER_ONE);
            }
        }

        //金额赋值
        if (isTax)
            doneAbst.setLoansMoney(detail.getNonTaxMoney()
                    .add(Objects.nonNull(detail.getTaxMoney()) ? detail.getTaxMoney() : BigDecimal.ZERO));
        else
            doneAbst.setLoansMoney(detail.getNonTaxMoney());

        doneAbst.setAccountId(detailByTemp.getAccountId());

        if (!ObjectUtils.isEmpty(detailByTemp.getAccount().getAccountAuxiliaryTypeIds()))
            doneAbst.setAuxTypeId(Long.valueOf(detailByTemp.getAccount().getAccountAuxiliaryTypeIds()));

        return doneAbst;
    }

    /**
     * 获取贷方税额摘要
     *
     * @param isTaxByDai 是否新增税额摘要
     * @param template   凭证模板
     */
    private InvoiceCertDto.InvoiceCertAbst getTaxAbst(boolean isTaxByDai, CertificateTemplate template, List<InvoiceDetail> detailList) {
        if (!isTaxByDai)
            return null;

        InvoiceCertDto.InvoiceCertAbst taxAbst = new InvoiceCertDto.InvoiceCertAbst();

        CertificateTemplateAbstract taxByTemp = getTaxByTemp(template);

        taxAbst.setCertificateAbstract(taxByTemp.getCertificateAbstract());
        taxAbst.setNo(taxByTemp.getAccount().getNo());
        taxAbst.setName(taxByTemp.getAccount().getName());
        taxAbst.setMoneyType(taxByTemp.getMoneyType());

        //贷方税额辅助核算赋值


        taxAbst.setLoansMoney(
                detailList.stream()
                        .map(d -> Objects.nonNull(d.getTaxMoney()) ? d.getTaxMoney() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));

        taxAbst.setAccountId(taxByTemp.getAccountId());

        return taxAbst;
    }

    /**
     * 获取税务的凭证预览摘要
     *
     * @param template 凭证模板
     */
    private CertificateTemplateAbstract getTaxByTemp(CertificateTemplate template) {
        return template.getTemplateAbstractList().stream()
                .filter(a -> a.getAccount().getDirection().equals(StringTip.SUBJ_DIRE_D))
                .filter(a -> a.getMoneyType().equals(NumberUtils.INTEGER_ONE))
                .findFirst()
                .get();
    }

    /**
     * 获取借方摘要
     *
     * @param template   凭证模板
     * @param isTaxByDai 是否新增税额摘要
     * @param detailList 贷方发票明细摘要
     * @param taxAbst    贷方税额摘要
     */
    private InvoiceCertDto.InvoiceCertAbst getJieAbst(CertificateTemplate template, boolean isTaxByDai, List<InvoiceCertDto.InvoiceCertAbst> detailList, InvoiceCertDto.InvoiceCertAbst taxAbst) {
        InvoiceCertDto.InvoiceCertAbst jieAbst = new InvoiceCertDto.InvoiceCertAbst();
        CertificateTemplateAbstract jieByTemp = getJieByTemp(template);
        jieAbst.setCertificateAbstract(jieByTemp.getCertificateAbstract());
        jieAbst.setNo(jieByTemp.getAccount().getNo());
        jieAbst.setName(jieByTemp.getAccount().getName());
        jieAbst.setMoneyType(jieByTemp.getMoneyType());

        if (isTaxByDai)
            jieAbst.setBorrowMoney(detailList.stream().map(InvoiceCertDto.InvoiceCertAbst::getLoansMoney).reduce(BigDecimal.ZERO, BigDecimal::add).add(taxAbst.getLoansMoney()));
        else
            jieAbst.setBorrowMoney(detailList.stream().map(InvoiceCertDto.InvoiceCertAbst::getLoansMoney).reduce(BigDecimal.ZERO, BigDecimal::add));

        jieAbst.setAccountId(jieByTemp.getAccountId());

        if (!ObjectUtils.isEmpty(jieByTemp.getAccount().getAccountAuxiliaryTypeIds())) {
            jieAbst.setAuxiliary(StringTip.JIE_AUX_ERR);
            jieAbst.setAbstAuxId(NumberUtils.LONG_MINUS_ONE);
            jieAbst.setAuxTypeId(Long.valueOf(jieByTemp.getAccount().getAccountAuxiliaryTypeIds()));
        }

        return jieAbst;
    }

    /**
     * 获取借方的凭证预览摘要
     *
     * @param template 凭证模板
     */
    private CertificateTemplateAbstract getJieByTemp(CertificateTemplate template) {
        return template.getTemplateAbstractList().stream()
                .filter(a -> a.getAccount().getDirection().equals(StringTip.SUBJ_DIRE_J))
                .findFirst()
                .get();
    }

    /**
     * 获取生成凭证预览的摘要列表
     *
     * @param abstListByTemp 凭证模板摘要列表
     * @param jieAbst        借方摘要
     * @param taxAbst        贷方税额摘要
     * @param detailList     贷方发票明细摘要列表
     */
    private List<InvoiceCertDto.InvoiceCertAbst> getAbstListByCert(List<CertificateTemplateAbstract> abstListByTemp, InvoiceCertDto.InvoiceCertAbst jieAbst, InvoiceCertDto.InvoiceCertAbst taxAbst, List<InvoiceCertDto.InvoiceCertAbst> detailList) {
        List<InvoiceCertDto.InvoiceCertAbst> doneAbstList = new ArrayList<>();

        for (CertificateTemplateAbstract abstByTemp : abstListByTemp) {
            switch (abstByTemp.getAccount().getDirection()) {
                case "借":
                    doneAbstList.add(jieAbst);
                    break;
                case "贷":
                    doneAbstList.addAll(getDaiAbst(abstByTemp.getMoneyType(), taxAbst, detailList));
                    break;
            }
        }

        return doneAbstList;
    }

    /**
     * 获取贷方摘要
     *
     * @param flag       取值类型：0.价税合计;1.税额;2.不含税金额;
     * @param taxAbst    贷方税额摘要
     * @param detailList 贷方发票明细摘要列表
     */
    private List<InvoiceCertDto.InvoiceCertAbst> getDaiAbst(Integer flag, InvoiceCertDto.InvoiceCertAbst taxAbst, List<InvoiceCertDto.InvoiceCertAbst> detailList) {
        switch (flag) {
            case 1:
                return Collections.singletonList(taxAbst);
            case 0:
            case 2:
                return detailList;
        }

        return Collections.emptyList();
    }

    /**
     * 获取正确的用于重置辅助核算的类型id列表
     *
     * @param useNumByAuxIdOfAux 辅助核算id和辅助核算使用次数的映射
     * @param nameByIdOfType     辅助核算类型id和辅助核算类型名称的映射
     */
    private Map<Long, String> getDoneResetAuxIdList(Map<String, Integer> useNumByAuxIdOfAux, Map<Long, String> nameByIdOfType) {
        useNumByAuxIdOfAux.entrySet().forEach(e -> {
            if (e.getValue() > NumberUtils.INTEGER_ZERO)
                nameByIdOfType.remove(Long.valueOf(e.getKey()));
        });

        return nameByIdOfType;
    }
}