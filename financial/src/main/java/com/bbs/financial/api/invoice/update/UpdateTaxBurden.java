package com.bbs.financial.api.invoice.update;

import cn.hutool.core.date.DateTime;
import com.bbs.Result;
import com.bbs.financial.converter.InvoiceConverter;
import com.bbs.financial.entity.TaxBurdenCal;
import com.bbs.financial.enums.*;
import com.bbs.financial.service.TaxBurdenCalService;
import com.bbs.financial.util.DateUtil;
import com.bbs.financial.util.LoginUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping
public class UpdateTaxBurden {
    @Resource
    private DataSourceTransactionManager transactionManager;

    @Resource
    private TransactionDefinition transactionDefinition;

    @Resource
    private TaxBurdenCalService calORM;

    @Resource
    private InvoiceConverter invConverter;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Param {
        /**
         * 当前期数毫秒数
         */
        private Long msec;

        /**
         * 发票明细
         */
        private List<Item> items;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        /**
         * 税负测算唯一标识符
         */
        private Long id;

        /**
         * 项目名称
         */
        private String item;

        /**
         * 张数
         */
        private Integer num;

        /**
         * (不含税)金额
         */
        private String money;

        /**
         * 税额
         */
        private String taxMoney;

        /**
         * 排序: {@link TaxBurdenCalItemEnum}
         */
        private Integer sort;

        /**
         * 税负测算类型: {@link TaxBurdenCalTypeEnum}
         */
        private Integer type;
    }

    @PostMapping("/invoice/taxBurden")
    public Result<Boolean> update(@RequestBody Param param) {
        clearItemId(param.getItems());

        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            removeTaxBurden(param.getMsec());

            List<TaxBurdenCal> toDBList = new ArrayList<>();
            for (Item item : param.getItems()) {
                TaxBurdenCal taxBurdenCal = invConverter.toEntity(item);
                taxBurdenCal.setId(null);
                taxBurdenCal.setDate(DateTime.of(param.getMsec()).toJdkDate());
                taxBurdenCal.setAccountingSetId(LoginUser.getLoginSetId());
                toDBList.add(taxBurdenCal);
            }
            calORM.saveBatch(toDBList);

            transactionManager.commit(transaction);
            return Result.success();
        } catch (Exception e) {
            transactionManager.rollback(transaction);
            throw new RuntimeException(e);
        }
    }

    private void clearItemId(List<Item> itemList) {
        itemList.forEach(i -> i.setId(null));
    }

    private Boolean removeTaxBurden(Long msec) {
        List<Long> dates = DateUtil.getMonthRange(msec);
        return calORM.lambdaUpdate()
                .eq(TaxBurdenCal::getAccountingSetId, LoginUser.getLoginSetId())
                .and(!ObjectUtils.isEmpty(dates), ext -> ext
                        .ge(TaxBurdenCal::getDate, new Date(dates.get(0)))
                        .lt(TaxBurdenCal::getDate, new Date(dates.get(1))))
                .remove();
    }
}