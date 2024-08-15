package com.clinic.controller;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bbs.Result;
import com.clinic.app.AppStockService;
import com.clinic.entity.*;
import com.clinic.enums.AdmissionStateEnum;
import com.clinic.enums.DrugExpiryStateEnum;
import com.clinic.enums.DrugStockRule;
import com.clinic.enums.StockStateEnum;
import com.clinic.service.*;
import com.clinic.util.LoginUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.clinic.enums.DrugExpiryStateEnum.ABOUT_EXPIRES;
import static com.clinic.enums.DrugExpiryStateEnum.EXPIRES;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ONE;
import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ZERO;

/**
 * 统计页面
 */
@Slf4j
@RestController("countClinicIndex")
@RequestMapping
public class CountController {

    @Resource
    private SettingsService settingsService;

    @Resource
    private SterilizeLogService sterilizeLogService;
    @Resource
    private DisinfectionLogService disinfectionLogService;
    @Resource
    private AdmissionLogService admissionLogService;
    @Resource
    private PayService payService;
    @Resource
    private StockUnitService stockUnitService;
    @Resource
    private StockBatchService stockBatchService;

    @GetMapping("/count")
    public Result<VO> count() {
        long queueNumber = 0;
        long currentDayTotalReceptionNumber = 0;
        Date now = new Date();
        // 库存不足
        DrugExpiryGroup drugExpiryGroup = countAndUpdateDrugExpiryState();
        Map<String, List<AdmissionLog>> map = admissionLogService.list(new LambdaQueryWrapper<AdmissionLog>()
                .eq(AdmissionLog::getUserId, LoginUser.getId())
                .and(ext -> ext
                        .ge(AdmissionLog::getCreateTime, DateUtil.beginOfMonth(now))
                        .lt(AdmissionLog::getCreateTime, DateUtil.beginOfMonth(DateUtil.offsetMonth(now, INTEGER_ONE)))
                )
        ).stream().collect(Collectors.groupingBy(log -> DateUtil.formatDate(log.getCreateTime())));

        if(map.size() > INTEGER_ZERO) {
            List<AdmissionLog> currentDayAdmissionLogs = map.get(DateUtil.formatDate(now));
            if(nonNull(currentDayAdmissionLogs)) {
                for (AdmissionLog log : currentDayAdmissionLogs) {
                    AdmissionStateEnum state = log.getState();
                    if(AdmissionStateEnum.RUN.equals(state)) {
                        queueNumber++;
                    }
                    currentDayTotalReceptionNumber++;
                }
            }
        }
        // 本月接诊人数（折线图数据）
        Map<String, Integer> singularMonthReceptionNumber = map.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entity -> entity.getValue().size()));

        String[] split = DateUtil.formatDate(now).split("-");
        String prefix = split[1] + "-";
        List<String> dateList = new ArrayList<>();
        List<Integer> numberList = new ArrayList<>();
        int currentMonthDayNumber = DateUtil.lengthOfMonth(DateUtil.month(now), DateUtil.isLeapYear(DateUtil.year(now)));
        int max = 0;
        for (int day = 1; day <= currentMonthDayNumber; day++) {
            String key = prefix + (day < 10 ? ("0" + day) : day);
            dateList.add(key);
            Integer number = singularMonthReceptionNumber.getOrDefault(split[0] + "-" + key, INTEGER_ZERO);
            if(number > max) max = number;
            numberList.add(number);
        }
        ReceptionPeopleNumberChartData receptionPeopleNumberChartData = new CountController.ReceptionPeopleNumberChartData(dateList, numberList, max);

        // 本月销售额（柱状图数据）
        List<Pay> currentMonthPayList = payService.lambdaQuery()
                .eq(Pay::getCreator, LoginUser.getId())
                .and(ext -> ext
                        .ge(Pay::getUpdateTime, DateUtil.beginOfMonth(now))
                        .lt(Pay::getUpdateTime, DateUtil.beginOfMonth(DateUtil.offsetMonth(now, INTEGER_ONE)))
                )
                .eq(Pay::getState, 1)
                .list();
        Map<Date, List<Pay>> singularMonthEveryDayPayMap = currentMonthPayList.stream()
                .collect(Collectors.groupingBy(Pay::getCreateTime));
        Map<String, BigDecimal> singularMonthEveryDayFeeMap = singularMonthEveryDayPayMap.entrySet().stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(DateUtil.formatDate(entry.getKey()), entry.getValue()))
                .collect(Collectors.groupingBy(AbstractMap.SimpleEntry::getKey))
                .entrySet().stream()
                .map(entry -> {
                    BigDecimal singularMonthFee = entry.getValue().stream().map(AbstractMap.SimpleEntry::getValue)
                            .flatMap(List::stream).map(Pay::getFee).reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new AbstractMap.SimpleEntry<>(entry.getKey(), singularMonthFee);
                }).collect(Collectors.toMap(
                        AbstractMap.SimpleEntry::getKey,
                        AbstractMap.SimpleEntry::getValue
                ));
        BigDecimal currentDayEarnings = singularMonthEveryDayFeeMap.getOrDefault(DateUtil.formatDate(now), BigDecimal.ZERO);

        List<BigDecimal> singularMonthSalesList = new ArrayList<>();
        BigDecimal maxEarnings = BigDecimal.ZERO;
        for (int day = 1; day <= currentMonthDayNumber; day++) {
            String key = prefix + (day < 10 ? ("0" + day) : day);
            BigDecimal number = singularMonthEveryDayFeeMap.getOrDefault(split[0] + "-" + key, BigDecimal.ZERO);
            if(number.compareTo(maxEarnings) == INTEGER_ONE) maxEarnings = number;
            singularMonthSalesList.add(number);
        }
        SingularMonthSalesChartData singularMonthSalesChartData = new SingularMonthSalesChartData(dateList, singularMonthSalesList, maxEarnings);

        int AboutExpiresDrugNumber = drugExpiryGroup.getAboutExpires().size();
        boolean existCriticalDrug = AboutExpiresDrugNumber > INTEGER_ZERO;
        int stockUnderDrugNumber = drugExpiryGroup.getStockShortage().size();
        boolean existUnderStockDrug = stockUnderDrugNumber  > INTEGER_ZERO;
        boolean isDisinfection = disinfectionLogService.lambdaQuery()
                .eq(DisinfectionLog::getUserId, LoginUser.getId())
                .eq(DisinfectionLog::getCreateTime, now)
                .exists();

        boolean isSterilize = sterilizeLogService.lambdaQuery()
                .eq(SterilizeLog::getUserId, LoginUser.getId())
                .eq(SterilizeLog::getCreateTime, now)
                .exists();
        VO vo = new VO(
                queueNumber,
                currentDayTotalReceptionNumber,
                currentDayEarnings,
                existCriticalDrug,
                existUnderStockDrug,
                isDisinfection,
                isSterilize,
                receptionPeopleNumberChartData,
                singularMonthSalesChartData,
                drugExpiryGroup
        );
        if(existCriticalDrug) vo.setExistCriticalDrugNumber(AboutExpiresDrugNumber);
        if(existUnderStockDrug) vo.setExistCriticalDrugNumber(stockUnderDrugNumber);
        return Result.success(vo);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class VO {

        /**
         * 正在接诊（待接诊数）
         */
        private Long queueNumber;

        /**
         * 今日总接诊数
         */
        private Long currentDayTotalReceptionNumber;

        /**
         * 今日收益
         */
        private BigDecimal currentDayEarnings;

        /**
         * 有无临期药品
         */
        private Boolean existCriticalDrug;

        /**
         * 临期药品数量
         */
        private Integer existCriticalDrugNumber;

        /**
         * 有无库存不足药品
         */
        private Boolean existUnderStockDrug;

        /**
         * 库存不足药品数量
         */
        private Integer existUnderStockDrugNumber;

        /**
         * 今日是否消杀
         */
        private Boolean isDisinfection;

        /**
         * 今日是否消毒
         */
        private Boolean isSterilize;

        private ReceptionPeopleNumberChartData receptionPeopleNumberChartData;

        private SingularMonthSalesChartData singularMonthSalesChartData;

        private DrugExpiryGroup drugExpiryGroup;

        public VO(
                Long queueNumber,
                Long currentDayTotalReceptionNumber,
                BigDecimal currentDayEarnings,
                Boolean existCriticalDrug,
                Boolean existUnderStockDrug,
                Boolean isDisinfection,
                Boolean isSterilize,
                ReceptionPeopleNumberChartData receptionPeopleNumberChartData,
                SingularMonthSalesChartData singularMonthSalesChartData,
                DrugExpiryGroup drugExpiryGroup
        ) {
            this.queueNumber = queueNumber;
            this.currentDayTotalReceptionNumber = currentDayTotalReceptionNumber;
            this.currentDayEarnings = currentDayEarnings;
            this.existCriticalDrug = existCriticalDrug;
            this.existUnderStockDrug = existUnderStockDrug;
            this.isDisinfection = isDisinfection;
            this.isSterilize = isSterilize;
            this.receptionPeopleNumberChartData = receptionPeopleNumberChartData;
            this.singularMonthSalesChartData = singularMonthSalesChartData;
            this.drugExpiryGroup = drugExpiryGroup;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReceptionPeopleNumberChartData {

        private List<String> dateList;

        private List<Integer> numberList;

        private Integer max;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SingularMonthSalesChartData {

        private List<String> dateList;

        private List<BigDecimal> numberList;

        private BigDecimal max;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DrugExpiryGroup {

        private List<StockBatch> normal;

        private List<StockBatch> aboutExpires;

        private List<StockBatch> expires;

        private List<StockBatch> stockShortage;
    }

    public DrugExpiryGroup countAndUpdateDrugExpiryState(){
        Settings settings = settingsService.getByUserId();
        Integer stockExpiryAlertMonth = settingsService.getUserSettingStockExpiryAlertMonth(settings);  //用户设置的库存药品过期提醒时间'

        List<StockBatch> allDrugBatch = stockBatchService.lambdaQuery()
                .eq(StockBatch::getUserId, LoginUser.getId())
                .list();

        List<StockBatch> expiresStateNormal = new ArrayList<>();    // 正常
        List<StockBatch> needUpdateExpiresStateToExpires = new ArrayList<>();  // 过期（需要 update DB）
        List<StockBatch> needUpdateExpiresStateToAbout = new ArrayList<>(); // 即将过期（需要 update DB）
        List<StockBatch> needUpdateStockStateToShortage = new ArrayList<>(); // 库存状态短缺（需要 update DB）
        allDrugBatch.forEach(drugBatch -> {
            Long stockNumber = drugBatch.getNumber();
            Long totalNumber = drugBatch.getTotalNumber();
            DrugStockRule stateCountRule = drugBatch.getStateCountRule();
            // 筛选出【库存状态】正常，且需要统计【库存预警状态】的库存批次
            if(StockStateEnum.NORMAL.equals(drugBatch.getState()) && !DrugStockRule.NOT_COUNT.equals(stateCountRule)) {
                Integer countVal = drugBatch.getCountVal();
                // 当库存数量不足【自定义阈值】时，提醒库存不足（库存数量按最小单位统计，如粒/片/袋等）
                if(DrugStockRule.MIN_UNIT_PERCENTAGE_CUSTOMIZE.equals(stateCountRule)) {
                    Integer countUnitId = drugBatch.getCountUnitId();
                    Integer unitId = drugBatch.getUnitId();
                    // 统计单位与库存单位是否一致，如果一致可以直接计算，否则需要将数量换算到一致的单位
                    if(countUnitId.equals(unitId)) {
                        if(stockNumber <= countVal) {
                            drugBatch.setState(StockStateEnum.SHORTAGE);
                            needUpdateStockStateToShortage.add(drugBatch);
                        }
                    } else {
                        // 取出当前库存批次，对应的库存单位（包含全部单位与进制）
                        List<StockUnit> currentStockBatchUnits = stockUnitService.lambdaQuery()
                                .eq(StockUnit::getBatchId, drugBatch.getId()).orderByAsc(StockUnit::getSort).list();
                        // 获取统计单位与库存单位
                        StockUnit countUnit = null;
                        StockUnit stockNumberUnit = null;
                        for (StockUnit stockUnit : currentStockBatchUnits) {
                            if (stockUnit.getUnitId().equals(countUnitId)) {
                                countUnit = stockUnit;
                            }
                            if (stockUnit.getUnitId().equals(unitId)) {
                                stockNumberUnit = stockUnit;
                            }
                        }
                        // 设置了统计单位后，才能进行计算
                        if(nonNull(countUnit) && nonNull(stockNumberUnit)) {
                            Integer countUnitSort = countUnit.getSort();
                            Integer stockNumberSort = stockNumberUnit.getSort();
                            long parentUnitStockNumber = 0L;
                            if(countUnit.getSort() < stockNumberUnit.getSort()) {
                                // 由库存单位（较大单位），向统计单位（较小单位）遍历（0, 统计单位, 库存单位）
                                for (int index = stockNumberSort; index > countUnitSort; index--) {
                                    parentUnitStockNumber = stockNumber / currentStockBatchUnits.get(index).getStepSize();
                                }
                            } else {
                                // 由统计单位（较大单位），向库存单位（较小单位）遍历（0, 库存单位, 统计单位）
                                for (int index = stockNumberSort; index < countUnitSort; index++) {
                                    //此处，需要 * 下一级单位的 stepSize
                                    // 比如，本单位为箱，有 50 箱，需要得出有多少瓶
                                    // （下一级单位瓶，下一级单位进制 stepSize：100，即 100 瓶 = 1 箱）
                                    // 总瓶数量 = 50 箱 * 100瓶
                                    parentUnitStockNumber = stockNumber * currentStockBatchUnits.get(index +1).getStepSize();
                                }
                            }
                            if(parentUnitStockNumber <= countVal) {
                                drugBatch.setState(StockStateEnum.SHORTAGE);
                                needUpdateStockStateToShortage.add(drugBatch);
                            }
                        }
                    }

                    // 当库存数量不足 20% 时，提醒库存不足（库存数量按最小单位统计，如粒/片/袋等）
                } else if(DrugStockRule.MIN_UNIT_PERCENTAGE_20.equals(stateCountRule)) {
                    if(stockNumber <= totalNumber * 0.2) {
                        drugBatch.setState(StockStateEnum.SHORTAGE);
                        needUpdateStockStateToShortage.add(drugBatch);
                    }

                    // 当库存数量不足 50% 时，提醒库存不足（库存数量按最小单位统计，如粒/片/袋等）
                } else if(DrugStockRule.MIN_UNIT_PERCENTAGE_50.equals(stateCountRule)) {
                    if(stockNumber <= totalNumber * 0.5) {
                        drugBatch.setState(StockStateEnum.SHORTAGE);
                        needUpdateStockStateToShortage.add(drugBatch);
                    }

                    // 当库存数量不足 80% 时，提醒库存不足（库存数量按最小单位统计，如粒/片/袋等）
                } else if(DrugStockRule.MIN_UNIT_PERCENTAGE_80.equals(stateCountRule)) {
                    if(stockNumber <= totalNumber * 0.8) {
                        drugBatch.setState(StockStateEnum.SHORTAGE);
                        needUpdateStockStateToShortage.add(drugBatch);
                    }
                }
            }
            // 筛选出【即将过期】与【已过期】的批次药品
            if(
                    DrugExpiryStateEnum.NORMAL.getCode().equals(drugBatch.getExpiryState()) ||
                            ABOUT_EXPIRES.getCode().equals(drugBatch.getExpiryState())
            ) {
                DrugExpiryStateEnum expiryState = AppStockService.computeDrugIsExpiry(drugBatch, stockExpiryAlertMonth);
                if(EXPIRES.equals(expiryState)) {
                    drugBatch.setExpiryState(EXPIRES.getCode());
                    needUpdateExpiresStateToExpires.add(drugBatch);
                } else if(ABOUT_EXPIRES.equals(expiryState)) {
                    drugBatch.setExpiryState(ABOUT_EXPIRES.getCode());
                    needUpdateExpiresStateToAbout.add(drugBatch);
                } else {
                    expiresStateNormal.add(drugBatch);
                }
            }
        });
        updateExpiresState(needUpdateExpiresStateToExpires, needUpdateExpiresStateToAbout);
        updateStockState(needUpdateStockStateToShortage);
        return new DrugExpiryGroup(expiresStateNormal, needUpdateExpiresStateToExpires, needUpdateExpiresStateToAbout, needUpdateStockStateToShortage);
    }

    private void updateExpiresState(List<StockBatch> needUpdateExpiresStateToExpires, List<StockBatch> needUpdateExpiresStateToAbout) {
        if(needUpdateExpiresStateToExpires.size() > INTEGER_ZERO) {
            stockBatchService.updateBatchById(needUpdateExpiresStateToExpires);
        }
        if(needUpdateExpiresStateToAbout.size() > INTEGER_ZERO) {
            stockBatchService.updateBatchById(needUpdateExpiresStateToAbout);
        }
    }

    private void updateStockState(List<StockBatch> needUpdateStockStateToShortage) {
        if(needUpdateStockStateToShortage.size() > INTEGER_ZERO) {
            stockBatchService.updateBatchById(needUpdateStockStateToShortage);
        }
    }
}
