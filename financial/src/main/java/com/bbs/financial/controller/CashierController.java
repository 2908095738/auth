package com.bbs.financial.controller;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.financial.dto.BaseMoneyByCashierDto;
import com.bbs.financial.dto.ConfirmTotalDto;
import com.bbs.financial.dto.IOTotalDto;
import com.bbs.financial.dto.SubjectsNameDto;
import com.bbs.financial.entity.*;
import com.bbs.financial.service.*;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.bbs.Result.success;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ONE;
import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ZERO;

/**
 * 出纳控制器
 */
@Slf4j
@RestController
@RequestMapping("/cashier")
public class CashierController {

    @Resource
    private AccountService accountService;

    @Resource
    private CertificateService certificateService;

    @Resource
    private ZhangHuService zhService;

    @Resource
    private PriceTypeService priceTypeService;

    /**
     * 查询入账科目列表
     */
    @GetMapping("/listSubjects")
    public Result<Page<SubjectsNameDto>> listSubjects(Long companyId, @RequestParam Integer current, @RequestParam Integer size) {
        Page<SubjectsNameDto> page = accountService.selectJoinListPage(new Page<>(current, size), SubjectsNameDto.class, new MPJLambdaWrapper<Account>()
                .select(Account::getId, Account::getNo, Account::getAccountName)
                .eq(Account::getCompanyId, INTEGER_ZERO)
                .or().eq(nonNull(companyId), Account::getCompanyId, companyId)
        );
        // 根据 no 中的 - 的数量，获取需要查询的科目 level
        // ps: value 的三元，可忽略，用于解决 IDEA Null 检查
        // ps:（无具体作用，该判断是否生效取决于 eq 的 isSearchNo; 如果生效，value 始终为 split.length - INTEGER_ONE）

        return success(page);
    }

    /**
     * 获取期初余额
     *
     * @param companyId 公司id
     * @param dateStr   时间字符串
     * @return
     */
    @GetMapping("/cert/oriMoney")
    public Result<Long> getOriMoney(@RequestParam Long companyId, @RequestParam(name = "date", required = false) String dateStr) {
        //计算期初余额
        List<Certificate> tmpList = getCertListByBefore(companyId, dateStr);
        Long oriMoeny = 0L;
        for (Certificate cert : tmpList) {
            List<CertificateAbstract> abstList = cert.getAbstracts();
            if (!ObjectUtils.isEmpty(abstList)) {
                for (CertificateAbstract abst : abstList) {
                    if (!ObjectUtils.isEmpty(abst.getBorrowMoney()))
                        oriMoeny = oriMoeny + abst.getBorrowMoney();
                    if (!ObjectUtils.isEmpty(abst.getLoansMoney()))
                        oriMoeny = oriMoeny - abst.getLoansMoney();

                }
            }
        }

        return Result.success(oriMoeny);
    }

    /**
     * 获取当月前的凭证列表
     *
     * @param companyId 公司
     * @param dateStr   时间字符串
     * @return
     */
    private List<Certificate> getCertListByBefore(Long companyId, String dateStr) {
        Date date = nonNull(dateStr) ? new Date(Long.parseLong(dateStr)) : new Date();
        return certificateService.listDeep(Wrappers.<Certificate>lambdaQuery()
                .eq(Certificate::getCompanyId, companyId)
                .lt(Certificate::getDate, DateUtil.beginOfMonth(date))
                .orderByDesc(Certificate::getDate)
        );
    }

    @GetMapping("/cert/iototal")
    public Result<Page<IOTotalDto>> listIOTotal(
            @RequestParam(defaultValue = "1") Integer current, @RequestParam(defaultValue = "10") Integer size,
            @RequestParam Long companyId,
            @RequestParam(name = "date", required = false) String dateStr, @RequestParam(name = "zhId", required = false) Long zhId) {
        Result result = Result.success();

        Page<IOTotalDto> page = getTotalByPart(current, size, companyId, zhId);
        result = fixTotalList(page.getRecords());
        if (result.getCode() == 500)
            return result;

        //科目id与汇总表的映射
        Map<Long, IOTotalDto> subjMap = page.getRecords().stream().collect(Collectors.toMap(IOTotalDto::getSubjectsId, d -> d));
        subjMap.values().forEach(d -> initMoneyByNow(Collections.singletonList(d)));

        //期初余额计算
        initMoney(getCertListByBefore(companyId, dateStr), sId -> subjMap.containsKey(sId), sId -> Collections.singletonList(subjMap.get(sId)), false);

        //收入、支出计算
        initMoney(getCertListByNow(companyId, dateStr), sId -> subjMap.containsKey(sId), sId -> Collections.singletonList(subjMap.get(sId)), true);

        //期末余额计算
        subjMap.values().forEach(d -> initEndMoney(Collections.singletonList(d)));

        //初始化币别名称
        result = initPriceTypeName(subjMap.values(), companyId);
        if (result.getCode() == 500)
            return result;

        result.setData(page);

        return result;
    }

    /**
     * 获取收支汇总表的部分数据列表
     *
     * @param current   页码
     * @param size      条数
     * @param companyId 公司id
     * @param zhId      账户id
     * @return
     */
    private Page<IOTotalDto> getTotalByPart(Integer current, Integer size, Long companyId, Long zhId) {
        return zhService.selectJoinListPage(new Page<>(current, size), IOTotalDto.class,
                new MPJLambdaWrapper<ZhangHu>()
                        .selectAs(ZhangHu::getCode, IOTotalDto::getZhCode)
                        .selectAs(ZhangHu::getName, IOTotalDto::getZhName)
                        .select(ZhangHu::getMTypeId, ZhangHu::getSubjectsId)
                        .eq(ZhangHu::getIsActive, 1)
                        .eq(ZhangHu::getCompanyId, companyId)
                        .eq(!ObjectUtils.isEmpty(zhId) && (zhId > NumberUtils.LONG_ZERO), ZhangHu::getId, zhId)
                        .orderByAsc(ZhangHu::getCreateTime)
        );
    }

    /**
     * 获取正确的收支汇总列表
     *
     * @param toFixList 待修复列表
     */
    private Result fixTotalList(List<IOTotalDto> toFixList) {
        //TODO L SQL待修正，即分组后只取首个

        //多账户可能会绑定同一账目，所以收支汇总表仅展示首位账户。
        Map<Long, List<IOTotalDto>> subjMap = toFixList.stream()
                .filter(t -> !ObjectUtils.isEmpty(t.getSubjectsId()))
                .collect(Collectors.groupingBy(IOTotalDto::getSubjectsId));
        toFixList.clear();

        if (ObjectUtils.isEmpty(subjMap))
            return Result.failed("zhanghu need have kemu");

        for (List<IOTotalDto> nowList : subjMap.values())
            toFixList.add(nowList.get(0));

        return Result.success();
    }

    /**
     * 初始化金额相关实例域
     *
     * @param certList  凭证列表
     * @param isProcess 科目id与汇总表的映射
     * @param isNow     true: 当月；false:本月之前
     */
    private void initMoney(List<Certificate> certList, Predicate<Long> isProcess, Function<Long, List<? extends BaseMoneyByCashierDto>> getDto, boolean isNow) {
        for (Certificate cert : certList) {
            List<CertificateAbstract> abstList = cert.getAbstracts();
            if (!ObjectUtils.isEmpty(abstList)) {
                Long subjId = abstList.get(0).getAccountId();
                if (isProcess.test(subjId)) {
                    getDto.apply(subjId).forEach(dto -> initMoneyByOne(abstList, dto, isNow));
                }
            }
        }
    }

    /**
     * 给当前收支汇总表的金额相关实例域赋初值
     *
     * @param dtoList 多态金额实例列表
     */
    private void initMoneyByNow(Collection<? extends BaseMoneyByCashierDto> dtoList) {
        dtoList.forEach(dto -> {
            dto.setOriMoney(new BigDecimal(0));
            dto.setBorrowMoney(new BigDecimal(0));
            dto.setLoansMoney(new BigDecimal(0));
        });
    }

    /**
     * 同一科目的收支汇总表数据赋值
     *
     * @param abstList 凭证摘要列表
     * @param dto      多态金额相关实例
     * @param isNow    true: 当月；false:本月之前
     */
    private void initMoneyByOne(List<CertificateAbstract> abstList, BaseMoneyByCashierDto dto, boolean isNow) {
        for (CertificateAbstract abst : abstList) {
            //收入、期初余额计算
            if (!ObjectUtils.isEmpty(abst.getBorrowMoney()) && abst.getBorrowMoney() > 0) {
                BigDecimal bMoneyByNow = new BigDecimal(abst.getBorrowMoney());

                if (isNow) {
                    dto.setBorrowMoney(dto.getBorrowMoney().add(bMoneyByNow));
                } else
                    dto.setOriMoney(dto.getOriMoney().add(bMoneyByNow));
            }

            //支出、期初余额计算
            if (!ObjectUtils.isEmpty(abst.getLoansMoney()) && abst.getLoansMoney() > 0) {
                BigDecimal lMoneyByNow = new BigDecimal(abst.getLoansMoney());

                if (isNow)
                    dto.setLoansMoney(dto.getLoansMoney().add(lMoneyByNow));
                else
                    dto.setOriMoney(dto.getOriMoney().subtract(lMoneyByNow));
            }
        }
    }

    /**
     * 获取当月凭证列表
     *
     * @param companyId 公司id
     * @param dateStr   时间字符串
     * @return
     */
    private List<Certificate> getCertListByNow(Long companyId, String dateStr) {
        Date date = nonNull(dateStr) ? new Date(Long.parseLong(dateStr)) : new Date();
        return certificateService.listDeep(Wrappers.<Certificate>lambdaQuery()
                .eq(Certificate::getCompanyId, companyId)
                .ge(Certificate::getDate, DateUtil.beginOfMonth(date))
                .lt(Certificate::getDate, DateUtil.beginOfMonth(DateUtil.offsetMonth(date, INTEGER_ONE)))
                .orderByDesc(Certificate::getDate)
        );
    }

    /**
     * 初始化期末余额
     *
     * @param dtoList 多态金额相关实例列表
     */
    private void initEndMoney(List<? extends BaseMoneyByCashierDto> dtoList) {
        dtoList.forEach(dto -> {
            BigDecimal oriMoney = dto.getOriMoney();
            BigDecimal bMoney = dto.getBorrowMoney();
            BigDecimal lMoney = dto.getLoansMoney();

            dto.setEndMoney(oriMoney.add(bMoney).subtract(lMoney));
        });
    }

    /**
     * 初始化币别名称
     *
     * @param dtos      收支汇总表列表
     * @param companyId 公司id
     */
    private Result initPriceTypeName(Collection<IOTotalDto> dtos, Long companyId) {
        List<Long> mTypeIds = dtos.stream().map(IOTotalDto::getMTypeId).collect(Collectors.toList());
        if (ObjectUtils.isEmpty(mTypeIds))
            return Result.failed("need create zhanghu");
        List<PriceType> mTypes = priceTypeService.selectJoinList(PriceType.class, new MPJLambdaWrapper<PriceType>()
                .select(PriceType::getId, PriceType::getName)
                .eq(PriceType::getCompanyId, companyId)
                .in(PriceType::getId, mTypeIds)
        );

        Map<Long, List<IOTotalDto>> mTypeMap = dtos.stream().collect(Collectors.groupingBy(IOTotalDto::getMTypeId));
        mTypes.forEach(t -> {
            List<IOTotalDto> dtoList = mTypeMap.get(t.getId());
            dtoList.forEach(d -> d.setMTypeName(t.getName()));
        });

        return Result.success();
    }

    /**
     * 获取核对总账列表
     *
     * @param current   页码
     * @param size      条数
     * @param companyId 公司id
     * @param msecStr   时间戳字符串
     * @return
     */
    @GetMapping("/cert/confirm")
    public Result<Page<ConfirmTotalDto>> listConfirm(
            @RequestParam(defaultValue = "1") Integer current, @RequestParam(defaultValue = "10") Integer size,
            @RequestParam Long companyId,
            @RequestParam(name = "msecStr", required = false) String msecStr) {
        Result result = Result.success();

        Page<ConfirmTotalDto> page = initConfrimPage(current, size, companyId);

        //科目id和核对总账的映射
        Map<Long, ConfirmTotalDto> subjMap = page.getRecords().stream()
                .filter(c -> !ObjectUtils.isEmpty(c.getSubj()))
                .collect(Collectors.toMap(c -> c.getSubj().getSubjectsId(), d -> d));
        if (ObjectUtils.isEmpty(subjMap))
            return Result.failed("zhanghu need have kemu");

        //科目id列表
        List<Long> subjIds = page.getRecords().stream().map(ConfirmTotalDto::getSubj).map(ConfirmTotalDto.SubjDto::getSubjectsId).collect(Collectors.toList());

        //不同时间区间的凭证列表
        List<Certificate> certsByBefore = getCertListByBefore(companyId, msecStr);
        List<Certificate> certsByBeNow = getCertListByNow(companyId, msecStr);

        //初始化科目实例
        initTotalByConfirm(subjMap, certsByBefore, certsByBeNow, c -> Collections.singletonList(c.getSubj()));
        initSubjName(subjIds, subjMap);

        //初始化子科目列表
        initDownSubj(subjIds, page.getRecords(), subjMap, certsByBefore, certsByBeNow);

        //初始化差额实例
        initEndMoney(subjMap.values());

        result.setData(page);

        return result;
    }

    /**
     * 初始化核对总账分页
     *
     * @param current   页码
     * @param size      条数
     * @param companyId 公司id
     * @return
     */
    private Page<ConfirmTotalDto> initConfrimPage(Integer current, Integer size, Long companyId) {
        //分页核心数据赋值
        Page<ConfirmTotalDto.SubjDto> tmpPage = getSubjIds(current, size, companyId);
        List<ConfirmTotalDto> records = new ArrayList<>();
        tmpPage.getRecords().forEach(s -> {
            ConfirmTotalDto now = new ConfirmTotalDto();
            now.setSubj(s);
            records.add(now);
        });

        //分页杂项数据赋值
        Page<ConfirmTotalDto> page = new Page<>();
        page.setCurrent(tmpPage.getCurrent());
        page.setSize(tmpPage.getSize());
        page.setTotal(tmpPage.getTotal());
        page.setRecords(records);

        return page;
    }

    /**
     * 获取科目id列表
     *
     * @param current   页码
     * @param size      条数
     * @param companyId 公司id
     * @return
     */
    private Page<ConfirmTotalDto.SubjDto> getSubjIds(Integer current, Integer size, Long companyId) {
        return zhService.selectJoinListPage(new Page<>(current, size), ConfirmTotalDto.SubjDto.class,
                new MPJLambdaWrapper<ZhangHu>()
                        .select(ZhangHu::getSubjectsId)
                        .eq(ZhangHu::getIsActive, 1)
                        .eq(ZhangHu::getCompanyId, companyId)
                        .groupBy(ZhangHu::getSubjectsId)
        );
    }

    /**
     * 初始化核对总账的科目实例
     *
     * @param subjMap       科目id和核对总账的映射
     * @param certsByBefore 本月前的凭证列表
     * @param certsByBeNow  本月的凭证列表
     * @param getMoneyImpl  获取多态金额实例
     */
    private <T> void initTotalByConfirm
    (Map<Long, T> subjMap, List<Certificate> certsByBefore, List<Certificate> certsByBeNow, Function<T, List<? extends
            BaseMoneyByCashierDto>> getMoneyImpl) {
        //金额相关实例域赋初值
        subjMap.values().forEach(d -> initMoneyByNow(getMoneyImpl.apply(d)));

        //期初余额计算
        initMoney(certsByBefore, sId -> subjMap.containsKey(sId), sId -> getMoneyImpl.apply(subjMap.get(sId)), false);

        //收入、支出计算
        initMoney(certsByBeNow, sId -> subjMap.containsKey(sId), sId -> getMoneyImpl.apply(subjMap.get(sId)), true);

        //期末余额计算
        subjMap.values().forEach(d -> initEndMoney(getMoneyImpl.apply(d)));
    }

    /**
     * 初始化科目总账名称
     *
     * @param subjIds 科目id列表
     * @param subjMap 科目id和核对总账的映射
     */
    private void initSubjName(List<Long> subjIds, Map<Long, ConfirmTotalDto> subjMap) {
        List<SubjectsNameDto> subjs = accountService.selectJoinList(SubjectsNameDto.class,
                new MPJLambdaWrapper<Account>()
                        .select(Account::getId, Account::getNo, Account::getAccountName)
                        .in(Account::getId, subjIds)
        );
        subjs.forEach(s -> {
            ConfirmTotalDto.SubjDto subj = subjMap.get(s.getId()).getSubj();
            subj.setNo(s.getNo());
            subj.setProjName(s.getAccountName());
        });
    }

    /**
     * 初始化子科目列表
     *
     * @param subjIds       科目id列表
     * @param upSubjList    [父]科目列表
     * @param subjMap       科目id和核对总账的映射
     * @param certsByBefore 当月前的凭证列表
     * @param certsByBeNow  当月凭证列表
     */
    private void initDownSubj
    (List<Long> subjIds, List<ConfirmTotalDto> upSubjList, Map<Long, ConfirmTotalDto> subjMap, List<Certificate> certsByBefore, List<Certificate> certsByBeNow) {
        initDownSubjByBase(subjIds, upSubjList);
        subjMap.values().forEach(c -> {
            List<ConfirmTotalDto.BaseTotalDto> zhTotals = c.getDownSubj();

            //科目总账id[父]和子科目总账的映射
            Map<Long, List<ConfirmTotalDto.BaseTotalDto>> subjForZh = zhTotals.stream().collect(Collectors.groupingBy(ConfirmTotalDto.BaseTotalDto::getSubjectsId));

            initTotalByConfirm(subjForZh, certsByBefore, certsByBeNow, z -> z);
        });
    }

    /**
     * 初始化子科目列表[基础]
     *
     * @param subjIds 科目id列表
     * @param dtos    核对总账列表
     */
    private void initDownSubjByBase(List<Long> subjIds, List<ConfirmTotalDto> dtos) {
        Map<Long, List<Account>> zhMap = getDownSubj(subjIds);

        dtos.forEach(c -> {
            List<ConfirmTotalDto.BaseTotalDto> zhTotals = new ArrayList<>();

            //初始化子科目
            Long subjId = c.getSubj().getSubjectsId();//科目id
            List<Account> zhList = zhMap.get(subjId);
            zhList.forEach(z -> {
                ConfirmTotalDto.BaseTotalDto zhTotal = new ConfirmTotalDto.BaseTotalDto();
                zhTotal.setProjName(z.getAccountName());
                zhTotal.setSubjectsId(z.getId());
                zhTotals.add(zhTotal);
            });

            c.setDownSubj(zhTotals);
        });
    }

    /**
     * 获取子科目分组[key: 父科目id]
     *
     * @param subjIds 科目id列表
     * @return
     */
    private Map<Long, List<Account>> getDownSubj(List<Long> subjIds) {
        List<Account> tmpZhs = accountService.selectJoinList(Account.class,
                new MPJLambdaWrapper<Account>()
                        .select(Account::getId, Account::getAccountName, Account::getParentId)
                        .in(Account::getParentId, subjIds)
                        .orderByAsc(Account::getWeight)
        );
        return tmpZhs.stream().collect(Collectors.groupingBy(Account::getParentId));
    }

    /**
     * 初始化差额
     *
     * @param confirmList 核对总账列表
     */
    private void initEndMoney(Collection<ConfirmTotalDto> confirmList) {
        confirmList.forEach(c -> {
            //差额
            ConfirmTotalDto.BaseTotalDto lessTotal = getEndMoney(c.getSubj());

            //金额计算
            if (!ObjectUtils.isEmpty(c.getDownSubj()))
                c.getDownSubj().forEach(d -> endMoneyCal(d, lessTotal));

            c.setLessTotal(lessTotal);
        });
    }

    /**
     * 获取差额
     *
     * @param upSubj [父]科目
     * @return
     */
    private ConfirmTotalDto.BaseTotalDto getEndMoney(ConfirmTotalDto.SubjDto upSubj) {
        ConfirmTotalDto.BaseTotalDto lessTotal = new ConfirmTotalDto.BaseTotalDto();
        lessTotal.setOriMoney(upSubj.getOriMoney());
        lessTotal.setBorrowMoney(upSubj.getBorrowMoney());
        lessTotal.setLoansMoney(upSubj.getLoansMoney());
        lessTotal.setEndMoney(upSubj.getEndMoney());
        lessTotal.setProjName("差额");
        return lessTotal;
    }

    /**
     * 差额计算并赋值
     *
     * @param subj      [父]科目总账
     * @param lessTotal 差额总账
     */
    private void endMoneyCal(ConfirmTotalDto.BaseTotalDto subj, ConfirmTotalDto.BaseTotalDto lessTotal) {
        //差额---期初余额
        endMoneyFunc(subj.getOriMoney(), lessTotal.getOriMoney(), m -> lessTotal.setOriMoney(m));
        //差额---收入余额
        endMoneyFunc(subj.getBorrowMoney(), lessTotal.getBorrowMoney(), m -> lessTotal.setBorrowMoney(m));
        //差额---支出余额
        endMoneyFunc(subj.getLoansMoney(), lessTotal.getLoansMoney(), m -> lessTotal.setLoansMoney(m));
        //差额---期末余额
        endMoneyFunc(subj.getEndMoney(), lessTotal.getEndMoney(), m -> lessTotal.setEndMoney(m));
    }

    /**
     * 差额计算并赋值[具体实现]
     *
     * @param subjMoney    [父]科目相应金额
     * @param lessMoney    差额总账相应金额
     * @param lessMoneySet 差额总账金额赋值
     */
    private void endMoneyFunc(BigDecimal subjMoney, BigDecimal lessMoney, Consumer<BigDecimal> lessMoneySet) {
        switch (subjMoney.compareTo(BigDecimal.ZERO)) {
            case 1:
                lessMoneySet.accept(lessMoney.subtract(subjMoney));
                break;
            case -1:
                lessMoneySet.accept(lessMoney.add(subjMoney));
                break;
        }
    }
}