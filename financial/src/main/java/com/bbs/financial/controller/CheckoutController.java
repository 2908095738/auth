package com.bbs.financial.controller;

import com.bbs.Result;
import com.bbs.financial.entity.Checkout;
import com.bbs.financial.service.CheckoutService;
import com.bbs.financial.util.LoginUser;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bbs.Result.success;

/**
 * 结账控制器
 */
@RestController
@RequestMapping("/check")
public class CheckoutController {

    @Resource
    private CheckoutService checkoutService;

    /**
     * 新增结账
     */
    @PostMapping
    public Result<Boolean> addCheck(@RequestBody Checkout checkout) {
        checkoutService.save(checkout);
        return success();
    }

    /**
     * 获取出纳启用期间
     */
    @GetMapping("/getOri")
    public Result<Date> getOriByCheck() {
        return checkoutService.getOriByCheck(LoginUser.getCompanyId());
    }

    /**
     * 获取本年结账列表
     *
     * @param msecStr   时间戳字符串
     */
    @GetMapping("/getCheckByYear")
    public Result<List<Checkout>> getCheckByYear( @RequestParam String msecStr) {
        Map<String, Date> dateMap = getDateByLoop(msecStr);
        return Result.success(checkoutService.getCheckByYear(dateMap.get("ori"), dateMap.get("end")));
    }

    /**
     * 获取本年起始、结束日的Date
     *
     * @return [key：ori、end]
     */
    private Map<String, Date> getDateByLoop(String msecStr) {
        Map<String, Date> result = new HashMap<>();

        //获取本年
        Date now = new Date(Long.parseLong(msecStr));
        Instant nowI = now.toInstant();
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDate nowLD = nowI.atZone(zoneId).toLocalDate();
        int year = nowLD.getYear();

        LocalDate oriLDByYear = LocalDate.of(year, 1, 1);
        LocalDate endLDByYear = LocalDate.of(year, 12, 31);
        ZonedDateTime oriZDT = oriLDByYear.atStartOfDay(zoneId);
        ZonedDateTime endZDT = endLDByYear.atStartOfDay(zoneId);
        Date oriD = Date.from(oriZDT.toInstant());
        Date endD = Date.from(endZDT.toInstant());

        result.put("ori", oriD);
        result.put("end", endD);

        return result;
    }

    /**
     * 本月是否结账
     *
     * @param msecStr   时间戳字符串
     */
    @GetMapping("/isCheck")
    public Result<Boolean> isCheck( @RequestParam String msecStr) {
        return Result.success(checkoutService.isCheck(msecStr));
    }

    /**
     * 修改结账
     */
    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody Checkout checkout) {
        checkoutService.updateById(checkout);
        return success();
    }
}