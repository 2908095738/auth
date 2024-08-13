package com.bbs.financial.api.invoice.search;

import com.bbs.Result;
import com.bbs.financial.converter.InvoiceConverter;
import com.bbs.financial.dto.TaxBurdenDto;
import com.bbs.financial.entity.Invoice;
import com.bbs.financial.entity.InvoiceDetail;
import com.bbs.financial.entity.TaxBurdenCal;
import com.bbs.financial.enums.InvStatusEnum;
import com.bbs.financial.enums.TaxBurdenCalItemEnum;
import com.bbs.financial.enums.TaxBurdenCalTypeEnum;
import com.bbs.financial.service.InvoiceService;
import com.bbs.financial.service.TaxBurdenCalService;
import com.bbs.financial.util.DateUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.bbs.financial.enums.TaxBurdenCalItemEnum.*;
import static com.bbs.financial.enums.TaxBurdenCalTypeEnum.*;

/**
 * 查询本期税负测算
 */
@RestController
@RequestMapping
public class SearchTaxBurden {

    @Resource
    private TaxBurdenCalService orm;

    @Resource
    private InvoiceService invORM;

    @Resource
    private InvoiceConverter invConverter;

    public static final class Constant {
        private static final Long CAL_TAX_FLAG = -2L;//计算[本期预计应交税额]的项目标识符

        private static final Long TAX_FLAG = -3L;//[本期预计应交税额]的项目标识符

        //下述的XXX_FLAG: 展示的UI中，具体是哪个类型跨多少行标识符
        private static final Long SALE_FLAG = -6L;

        private static final Long IN_FLAG = -7L;

        private static final Long VAT_FLAG = -8L;

        private static final Long PLUS_FLAG = -9L;
    }

    /**
     * 查询本期税负测算
     *
     * @param msec 当前期数毫秒数
     */
    @GetMapping("/invoice/taxBurden/{msec}")
    public Result<TaxBurdenDto> search(@PathVariable Long msec) {
        TaxBurdenDto resultDto = new TaxBurdenDto();

        List<Long> msecList = DateUtil.getMonthRange(msec);

        //类型下的项目列表和类型的映射
        Map<TaxBurdenCalTypeEnum, List<TaxBurdenCal>> typeListMap = getTypeListMap(msecList);

        resultDto.setSaleList(getSaleList(msecList, typeListMap));
        resultDto.setInList(getInItemList(msecList, typeListMap));
        resultDto.setVatList(getVatList(resultDto.getSaleList(), resultDto.getInList(), typeListMap));
        resultDto.setPlusTaxList(getPlusTaxList(resultDto.getVatList(), typeListMap));

        return Result.success(resultDto);
    }

    /**
     * 获取类型下的项目列表和类型的映射
     *
     * @param msecList 当前期数开始、结束时间戳列表
     */
    private Map<TaxBurdenCalTypeEnum, List<TaxBurdenCal>> getTypeListMap(List<Long> msecList) {
        Map<TaxBurdenCalTypeEnum, List<TaxBurdenCal>> typeListByType = Collections.emptyMap();
        List<TaxBurdenCal> burdenCalList = orm.search(msecList);
        if (!burdenCalList.isEmpty())
            typeListByType = burdenCalList.stream().collect(Collectors.groupingBy(TaxBurdenCal::getType));
        return typeListByType;
    }

    /**
     * @param msecList    当前期数开始、结束时间戳列表
     * @param typeListMap 类型下的项目列表和类型的映射
     */
    private List<TaxBurdenDto.TaxBurdenBase> getSaleList(List<Long> msecList, Map<TaxBurdenCalTypeEnum, List<TaxBurdenCal>> typeListMap) {
        List<TaxBurdenDto.TaxBurdenBase> saleList = new ArrayList<>();
        initSaleItemByInv(msecList, saleList);

        //新增销项-手填项目
        saleList.add(getItem(typeListMap.get(SALE), SALE_ADD_NO.getMsg(), SALE.getCode(), SALE_ADD_NO.getSort()));
        saleList.add(getItem(typeListMap.get(SALE), SALE_SUB.getMsg(), SALE.getCode(), SALE_SUB.getSort()));
        saleList.add(getItem(typeListMap.get(SALE), SALE_ADD_OTHER.getMsg(), SALE.getCode(), SALE_ADD_OTHER.getSort()));

        //新增销项-小计项目
        saleList.add(getTotalByCalTax(saleList, SALE_TOTAL.getMsg()));

        return saleList;
    }

    /**
     * 初始化需要用发票数据初始化的销项列表
     *
     * @param msecList 当前期数开始、结束时间戳列表
     * @param saleList 销项列表
     */
    private void initSaleItemByInv(List<Long> msecList, List<TaxBurdenDto.TaxBurdenBase> saleList) {
        //源数据
        List<Invoice> saleNormalList = invORM.searchNormalAndRedBySale(msecList);

        saleList.add(getItemByInvList(saleNormalList, i -> InvStatusEnum.NORMAL.equals(i.getInvoiceStatus()),
                Constant.SALE_FLAG, SALE_NORMAL));

        saleList.add(getItemByInvList(saleNormalList, i -> InvStatusEnum.RED.equals(i.getInvoiceStatus()),
                NumberUtils.LONG_MINUS_ONE, SALE_RED));
    }

    /**
     * 从发票列表中获取项目
     *
     * @param invList             发票列表
     * @param fieldPlusConditions 实例域值累加条件接口
     * @param id                  项目id
     * @param itemEnum            税负测算项目枚举
     */
    private TaxBurdenDto.TaxBurdenBase getItemByInvList(List<Invoice> invList, Predicate<Invoice> fieldPlusConditions,
                                                        Long id, TaxBurdenCalItemEnum itemEnum) {
        Integer num = 0;
        BigDecimal money = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;

        for (Invoice inv : invList) {
            if (fieldPlusConditions.test(inv)) {
                num += 1;
                for (InvoiceDetail detail : inv.getDetails()) {
                    money = money.add(detail.getNonTaxMoney());
                    tax = tax.add(getValidMoney(detail.getTaxMoney()));
                }
            }
        }

        TaxBurdenDto.TaxBurdenBase item = new TaxBurdenDto.TaxBurdenBase();
        item.setId(id);
        item.setItem(itemEnum.getMsg());
        item.setNum(num > NumberUtils.INTEGER_ZERO ? num : null);
        item.setMoney(getMoney(money));
        item.setTaxMoney(getMoney(tax));

        return item;
    }

    private BigDecimal getMoney(BigDecimal money) {
        return money.doubleValue() != NumberUtils.DOUBLE_ZERO.doubleValue() ? money : null;
    }

    /**
     * 获取指定类型下的具体项目
     *
     * @param itemListByType 当前类型下的项目列表
     * @param item           项目名称
     * @param type           税负测算类型枚举
     * @param sort           税负测算排序
     */
    private TaxBurdenDto.TaxBurdenBase getItem(List<TaxBurdenCal> itemListByType, String item, Integer type, Integer sort) {
        TaxBurdenDto.TaxBurdenBase result = null;

        //从DB查询赋值
        if (!ObjectUtils.isEmpty(itemListByType)) {
            for (TaxBurdenCal now : itemListByType) {
                TaxBurdenCalTypeEnum inTypeEnum = TaxBurdenCalTypeEnum.enumMap.get(type);
                boolean isType = now.getType().equals(inTypeEnum);
                if (isType && (now.getSort().equals(sort))) result = invConverter.toDto(now);
            }
        }

        //DB未查询到，自行初始化
        if (Objects.isNull(result)) {
            result = new TaxBurdenDto.TaxBurdenBase();
            result.setItem(item);
            result.setType(type);
            result.setSort(sort);
        }

        return result;
    }

    /**
     * 获取用于计算[本期预计应交税额]的小计项目
     *
     * @param itemList 项目列表
     * @param itemName 项目名称
     */
    private TaxBurdenDto.TaxBurdenBase getTotalByCalTax(List<TaxBurdenDto.TaxBurdenBase> itemList, String itemName) {
        TaxBurdenDto.TaxBurdenBase total = getTotalByType(itemList, itemName);
        total.setId(Constant.CAL_TAX_FLAG);
        return total;
    }

    /**
     * 获取当前类型下的小计项目
     *
     * @param itemList 项目列表
     * @param itemName 项目名称
     */
    private TaxBurdenDto.TaxBurdenBase getTotalByType(List<TaxBurdenDto.TaxBurdenBase> itemList, String itemName) {
        //初始化金额相关
        BigDecimal moneyTotal = BigDecimal.ZERO;
        BigDecimal taxTotal = BigDecimal.ZERO;

        //累加金额
        for (TaxBurdenDto.TaxBurdenBase item : itemList) {
            moneyTotal = moneyTotal.add(getValidMoney(item.getMoney()));
            taxTotal = taxTotal.add(getValidMoney(item.getTaxMoney()));
        }

        //初始化小计实例
        TaxBurdenDto.TaxBurdenBase total = new TaxBurdenDto.TaxBurdenBase();
        total.setId(NumberUtils.LONG_MINUS_ONE);
        total.setItem(itemName);
        total.setMoney(getMoney(moneyTotal));
        total.setTaxMoney(getMoney(taxTotal));

        return total;
    }

    private BigDecimal getValidMoney(BigDecimal inNum) {
        return Objects.nonNull(inNum) ? inNum : BigDecimal.ZERO;
    }

    /**
     * @param msecList    当前期数开始、结束时间戳列表
     * @param typeListMap 类型下的项目列表和类型的映射
     */
    private List<TaxBurdenDto.TaxBurdenBase> getInItemList(List<Long> msecList, Map<TaxBurdenCalTypeEnum, List<TaxBurdenCal>> typeListMap) {
        List<TaxBurdenDto.TaxBurdenBase> inItemList = new ArrayList<>();
        initInItemByInv(msecList, inItemList);

        //减: 进项转出待实现
        TaxBurdenDto.TaxBurdenBase item2 = new TaxBurdenDto.TaxBurdenBase();
        item2.setItem(IN_SUB_OUT.getMsg());
        inItemList.add(item2);

        //新增进项-手填项目
        inItemList.add(getItem(typeListMap.get(IN), IN_ADD_TAX.getMsg(), IN.getCode(), IN_ADD_TAX.getSort()));
        inItemList.add(getItem(typeListMap.get(IN), IN_SUB_AUTH.getMsg(), IN.getCode(), IN_SUB_AUTH.getSort()));

        //新增进项-小计项目
        inItemList.add(getTotalByCalTax(inItemList, IN_TOTAL.getMsg()));

        return inItemList;
    }

    /**
     * 获取需要用发票数据初始化的进项列表
     *
     * @param msecList 当前期数开始、结束时间戳列表
     */
    private void initInItemByInv(List<Long> msecList, List<TaxBurdenDto.TaxBurdenBase> inItemList) {
        //源数据
        List<Invoice> authList = invORM.searchAuthByInItem(msecList);
        List<Invoice> toDeductList = invORM.searchFeesInvoice(msecList);

        inItemList.add(getItemByInvList(authList, Invoice::getIsAuth,
                Constant.IN_FLAG, IN_AUTH));

        inItemList.add(getItemByInvList(toDeductList, i -> {
                    InvoiceDetail detail = i.getDetails().get(0);
                    if (Objects.isNull(detail.getTaxRates()))
                        return false;
                    return true;
                },
                NumberUtils.LONG_MINUS_ONE, IN_ADD_INV));
    }

    /**
     * 获取应交增值税类型下的项目列表
     *
     * @param saleList    销项下的项目列表
     * @param inList      进项下的项目列表
     * @param typeListMap 类型下的项目列表和类型的映射
     */
    private List<TaxBurdenDto.TaxBurdenBase> getVatList(List<TaxBurdenDto.TaxBurdenBase> saleList, List<TaxBurdenDto.TaxBurdenBase> inList, Map<TaxBurdenCalTypeEnum, List<TaxBurdenCal>> typeListMap) {
        int lastSaleIndex = saleList.size() - 1;
        int lastInItemIndex = inList.size() - 1;
        TaxBurdenDto.TaxBurdenBase saleTotalItem = saleList.get(lastSaleIndex);
        TaxBurdenDto.TaxBurdenBase inTotal = inList.get(lastInItemIndex);
        return getVatList(typeListMap, getNonNullMoney(saleTotalItem.getTaxMoney()), getNonNullMoney(inTotal.getTaxMoney()));
    }

    private BigDecimal getNonNullMoney(BigDecimal money) {
        return Objects.nonNull(money) ? money : BigDecimal.ZERO;
    }

    /**
     * 获取应交增值税项目列表
     *
     * @param typeListMap    类型下的项目列表和类型的映射
     * @param saleTaxTotal   销项类型小计税额
     * @param inItemTaxTotal 进项类型小计税额
     */
    private List<TaxBurdenDto.TaxBurdenBase> getVatList(Map<TaxBurdenCalTypeEnum, List<TaxBurdenCal>> typeListMap, BigDecimal saleTaxTotal, BigDecimal inItemTaxTotal) {
        List<TaxBurdenDto.TaxBurdenBase> vatList = new ArrayList<>();

        //新增应交增值税-期初留抵
        TaxBurdenDto.TaxBurdenBase vatOri = getItem(typeListMap.get(VAT), VAT_ORI.getMsg(), VAT.getCode(), VAT_ORI.getSort());
        vatOri.setId(Constant.VAT_FLAG);
        vatList.add(vatOri);

        //预计减免税待实现
        TaxBurdenDto.TaxBurdenBase item = new TaxBurdenDto.TaxBurdenBase();
        item.setItem(VAT_SUB.getMsg());
        item.setSort(VAT_SUB.getSort());
        vatList.add(item);

        //新增应交增值税-手填项目-税额
        vatList.add(getItem(typeListMap.get(VAT), VAT_DEVICE_NUM.getMsg(), VAT.getCode(), VAT_DEVICE_NUM.getSort()));
        vatList.add(getItem(typeListMap.get(VAT), VAT_TAX_NUM.getMsg(), VAT.getCode(), VAT_TAX_NUM.getSort()));

        //新增应交增值税-手填项目-项目-税额
        vatList.add(getFixItem(typeListMap, VAT, VAT_3_INPUT));
        vatList.add(getFixItem(typeListMap, VAT, VAT_4_INPUT));
        vatList.add(getFixItem(typeListMap, VAT, VAT_5_INPUT));

        //新增应交增值税-本期预计应交税额
        TaxBurdenDto.TaxBurdenBase mayTaxItem = getMayTax(saleTaxTotal, inItemTaxTotal, vatList);
        mayTaxItem.setId(Constant.TAX_FLAG);
        mayTaxItem.setMoney(null);//本期预计应交税额不需要显示金额
        mayTaxItem.setSort(VAT_MAYBE_TAX.getSort());
        vatList.add(mayTaxItem);

        //本期预计增值税税负率待实现
        TaxBurdenDto.TaxBurdenBase item2 = new TaxBurdenDto.TaxBurdenBase();
        item2.setItem(VAT_MAYBE_PLUS_TAX.getMsg());
        item2.setSort(VAT_MAYBE_PLUS_TAX.getSort());
        vatList.add(item2);

        //期末留抵税额待实现
        TaxBurdenDto.TaxBurdenBase item3 = new TaxBurdenDto.TaxBurdenBase();
        item3.setItem(VAT_END.getMsg());
        item3.setSort(VAT_END.getSort());
        vatList.add(item3);

        return vatList;
    }

    /**
     * 获取本期预计应交税额
     *
     * @param saleTaxTotal   销项税额小计
     * @param inItemTaxTotal 进项税额小计
     * @param vatList        应交增值税项目列表
     */
    private TaxBurdenDto.TaxBurdenBase getMayTax(BigDecimal saleTaxTotal, BigDecimal inItemTaxTotal, List<TaxBurdenDto.TaxBurdenBase> vatList) {
        //税额初始化
        BigDecimal doneMaybeTax = saleTaxTotal.subtract(inItemTaxTotal);

        //税额赋值
        TaxBurdenDto.TaxBurdenBase maybeTaxItem = getTotalByType(vatList, VAT_MAYBE_TAX.getMsg());
        maybeTaxItem.setTaxMoney(getMoney(doneMaybeTax.subtract(getNonNullMoney(maybeTaxItem.getTaxMoney()))));

        return maybeTaxItem;
    }

    /**
     * 获取附加税列表
     *
     * @param vatList     应交增值税类型下的项目列表
     * @param typeListMap 类型下的项目列表和类型的映射
     */
    private List<TaxBurdenDto.TaxBurdenBase> getPlusTaxList(List<TaxBurdenDto.TaxBurdenBase> vatList, Map<TaxBurdenCalTypeEnum, List<TaxBurdenCal>> typeListMap) {
        TaxBurdenDto.TaxBurdenBase maybeTaxItem = vatList.get(VAT_MAYBE_TAX.getSort() - 1);
        return getPlusTaxListCore(typeListMap, maybeTaxItem.getTaxMoney());
    }

    /**
     * 获取附加税列表
     *
     * @param typeListMap   类型下的项目列表和类型的映射
     * @param maybeTaxMoney 本期预计应交税额
     */
    private List<TaxBurdenDto.TaxBurdenBase> getPlusTaxListCore(Map<TaxBurdenCalTypeEnum, List<TaxBurdenCal>> typeListMap, BigDecimal maybeTaxMoney) {
        List<TaxBurdenDto.TaxBurdenBase> plusTaxList = new ArrayList<>();

        //新增附加税-手填项目
        {
            TaxBurdenDto.TaxBurdenBase cityItem = getPlusTaxItem(typeListMap, ADD_TAX, PLUS_CITY_NUM, maybeTaxMoney);
            cityItem.setId(Constant.PLUS_FLAG);
            plusTaxList.add(cityItem);

            plusTaxList.add(getPlusTaxItem(typeListMap, ADD_TAX, PLUS_EDU_NUM, maybeTaxMoney));
            plusTaxList.add(getPlusTaxItem(typeListMap, ADD_TAX, PLUS_AREA_NUM, maybeTaxMoney));
        }

        //新增附加税-小计项目
        plusTaxList.add(getTotalByType(plusTaxList, PLUS_TOTAL.getMsg()));

        return plusTaxList;
    }

    /**
     * 获取附加税类型下的项目
     *
     * @param typeListMap   类型下的项目列表和类型的映射
     * @param typeEnum      税负测算类型枚举
     * @param itemEnum      税负测算项目枚举
     * @param maybeTaxMoney 本期预计应交税额
     */
    private TaxBurdenDto.TaxBurdenBase getPlusTaxItem(Map<TaxBurdenCalTypeEnum, List<TaxBurdenCal>> typeListMap, TaxBurdenCalTypeEnum typeEnum, TaxBurdenCalItemEnum itemEnum, BigDecimal maybeTaxMoney) {
        TaxBurdenDto.TaxBurdenBase item = getFixItem(typeListMap, typeEnum, itemEnum);

        //初始化税额
        if (StringUtils.isNotBlank(item.getItem())) {
            if (item.getItem().matches("^[0-9]*$")) {
                BigDecimal moneyByItem = new BigDecimal(item.getItem());
                moneyByItem = moneyByItem.multiply(BigDecimal.valueOf(0.01));
                item.setTaxMoney(moneyByItem.multiply(maybeTaxMoney).setScale(2, BigDecimal.ROUND_HALF_EVEN));
            }
        }

        return item;
    }

    /**
     * 当前项目未入库，则把项目名称赋空，避免枚举的msg给UI造成的困扰。
     *
     * @param typeListMap 类型下的项目列表和类型的映射
     * @param typeEnum    税负测算类型枚举
     * @param itemEnum    税负测算项目枚举
     */
    private TaxBurdenDto.TaxBurdenBase getFixItem(Map<TaxBurdenCalTypeEnum, List<TaxBurdenCal>> typeListMap, TaxBurdenCalTypeEnum typeEnum, TaxBurdenCalItemEnum itemEnum) {
        TaxBurdenDto.TaxBurdenBase item = getItem(typeListMap.get(typeEnum), itemEnum.getMsg(), typeEnum.getCode(), itemEnum.getSort());
        if (Objects.isNull(item.getId()))//如果id为空说明未入库，项目名称此时是枚举中的msg，所有需要赋空，避免影响UI显示。
            item.setItem(null);
        return item;
    }
}