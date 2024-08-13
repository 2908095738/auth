package com.bbs.financial.api.invoice.update;

import com.bbs.Result;
import com.bbs.financial.converter.InvoiceConverter;
import com.bbs.financial.entity.Invoice;
import com.bbs.financial.entity.InvoiceDetail;
import com.bbs.financial.enums.InvCateEnum;
import com.bbs.financial.enums.InvStatusEnum;
import com.bbs.financial.enums.InvTypeEnum;
import com.bbs.financial.enums.TaxTypeEnum;
import com.bbs.financial.service.InvoiceDetailService;
import com.bbs.financial.service.InvoiceService;
import com.bbs.financial.util.LoginUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping
public class UpdateInvoice {

    @Resource
    private DataSourceTransactionManager transactionManager;

    @Resource
    private TransactionDefinition transactionDefinition;

    @Resource
    private InvoiceConverter invoiceConverter;

    @Resource
    private InvoiceService orm;

    @Resource
    private InvoiceDetailService detailORM;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Param {

        /**
         * 发票唯一标识符
         */
        private Long id;

        /**
         * 开票日期
         */
        private Date openDate;

        /**
         * 发票代码
         */
        private String invoiceCode;

        /**
         * 发票号码
         */
        private String invoiceNumber;

        /**
         * 发票状态：{@link InvStatusEnum}
         */
        private Integer invoiceStatus;

        /**
         * 客户名称
         */
        private String clientName;

        /**
         * 统一社会信用代码
         */
        private String creditCode;

        /**
         * 地址及电话
         */
        private String addressPhone;

        /**
         * 开户行及账户
         */
        private String openAccount;

        /**
         * 校验码后六位
         */
        private String verifyCode;

        /**
         * 备注
         */
        private String remark;

        /**
         * 录入发票明细：0.录入;1.不录入;
         */
        private Boolean isInvoiceDetail;

        /**
         * 发票类型：{@link InvTypeEnum}
         */
        private Integer invoiceType;

        /**
         * 发票分类：{@link InvCateEnum}
         */
        private Integer invoiceCategory;

        /**
         * 计税方式：{@link TaxTypeEnum}
         */
        private Integer taxType;

        /**
         * 发票明细
         */
        private List<DetailParam> details;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailParam {

        /**
         * 发票明细唯一标识符
         */
        private Long id;

        /**
         * 商品名称
         */
        private String name;

        /**
         * 规格型号/车牌号/车辆识别代号
         */
        private String code;

        /**
         * 单位
         */
        private String unit;

        /**
         * 数量
         */
        private Integer quantity;

        /**
         * 单价
         */
        private String price;

        /**
         * 税率
         */
        private String taxRates;

        /**
         * 不含税金额
         */
        private String nonTaxMoney;

        /**
         * 税额
         */
        private String taxMoney;

        /**
         * 发票辅助核算id
         */
        private Long abstAuxId;
    }

    @PostMapping("/invoice")
    public Result<Boolean> update(@RequestBody Param param) {
        Invoice invoice = invoiceConverter.toEntity(param);
        invoice.setIsInvoiceDetail(param.getIsInvoiceDetail() ? NumberUtils.INTEGER_ONE : NumberUtils.INTEGER_ZERO);
        invoice.setUpdateBy(LoginUser.getId());

        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            orm.lambdaUpdate()
                    .eq(Invoice::getId, invoice.getId())
                    .update(invoice);
            transactionManager.commit(transaction);
        } catch (Exception e) {
            transactionManager.rollback(transaction);
            throw new RuntimeException(e);
        }

        return updateDetailList(param);
    }

    /**
     * 更新发票明细
     */
    @PostMapping("/invoice/detail")
    public Result<Boolean> updateDetailList(@RequestBody Param invParam) {
        List<InvoiceDetail> detailListBy2DB = getTrueDetailList(invParam.getDetails(), invParam.getInvoiceType()).stream().map(invoiceConverter::toEntity).collect(Collectors.toList());
        detailListBy2DB.forEach(d -> d.setInvoiceId(invParam.getId()));

        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            List<DetailParam> oriList = invParam.getDetails();
            int flag = getInvDetailFlag(oriList, invParam.getInvoiceType());
            orm.lambdaUpdate()
                    .set(Invoice::getIsInvoiceDetail, flag)
                    .eq(Invoice::getId, invParam.getId())
                    .update();

            if (oriList.size() != detailListBy2DB.size()) {//生成凭证修改明细
                updByCert(invParam.getId(), detailListBy2DB);
            } else {
                detailORM.lambdaUpdate()
                        .eq(InvoiceDetail::getInvoiceId, invParam.getId())
                        .remove();

                detailORM.saveBatch(detailListBy2DB);
            }

            transactionManager.commit(transaction);
            return Result.success();
        } catch (Exception e) {
            transactionManager.rollback(transaction);
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取正确的待入库发票明细列表
     *
     * @param detailList  发票明细列表
     * @param invoiceType {@link InvTypeEnum} 发票类型
     */
    private List<DetailParam> getTrueDetailList(List<DetailParam> detailList, Integer invoiceType) {
        List<DetailParam> doneList = new ArrayList<>();

        if (!detailList.isEmpty() && detailList.size() < NumberUtils.INTEGER_TWO) {//发票更新为不录入发票明细
            DetailParam param = detailList.get(0);
            DetailParam detail = getTrueDetailByOne(param, invoiceType);
            if (Objects.nonNull(detail))
                doneList.add(detail);
        } else
            initTrueDetailByMore(detailList, doneList);

        return doneList;
    }

    /**
     * 获取正确的待入库发票明细
     *
     * @param param       发票明细实例
     * @param invoiceType {@link InvTypeEnum} 发票类型
     */
    private DetailParam getTrueDetailByOne(DetailParam param, Integer invoiceType) {
        if (StringUtils.isNotBlank(param.getPrice()) && invoiceType.equals(InvTypeEnum.FLY.getCode())) {
            //费用小票-发票类型_飞机票-机票+燃油费-发票明细-新增
            param.setPrice(param.getPrice());
            return param;
        } else if (!ObjectUtils.isEmpty(param.getNonTaxMoney())) {
            //判断条件：null说明生成凭证且需要修改的明细为借方金额，则不予入库。

            param.setId(null);

            //true: 生成凭证-贷方金额，false: 更新发票明细列表
            if (Objects.isNull(param.getQuantity())) {
                param.setQuantity(NumberUtils.INTEGER_ONE);
                param.setPrice(param.getNonTaxMoney());
            }

            return param;
        }

        return null;
    }

    /**
     * 初始化正确的待入库发票明细
     *
     * @param detailList 发票明细列表
     * @param doneList   正确的待入库发票明细列表
     */
    private void initTrueDetailByMore(List<DetailParam> detailList, List<DetailParam> doneList) {
        /**
         * 1.单纯修改发票的发票明细列表
         * 2.生成凭证修改发票明细时，需要剔除其中无用的借方金额，因为不包含在发票明细中
         */
        for (DetailParam param : detailList) {
            if (!ObjectUtils.isEmpty(param.getId())) {//排除借方金额
                if (param.getId() < NumberUtils.INTEGER_ONE)//前端新增明细时，id会是负数，需要重新赋值。
                    param.setId(null);

                //true: 生成凭证-贷方金额，false: 更新发票明细列表
                if (Objects.isNull(param.getQuantity())) {
                    param.setQuantity(NumberUtils.INTEGER_ONE);
                    param.setPrice(param.getNonTaxMoney());
                }

                doneList.add(param);
            }
        }
    }

    /**
     * 获取录入发票明细标识符：
     *
     * @param oriList     原始发票明细列表
     * @param invoiceType 发票分类：{@link InvTypeEnum}
     * @return 1.录入;0.不录入;
     */
    private int getInvDetailFlag(List<DetailParam> oriList, Integer invoiceType) {
        int flag = NumberUtils.INTEGER_ONE;

        if (!oriList.isEmpty() && oriList.size() < NumberUtils.INTEGER_TWO) {
            DetailParam param = oriList.get(NumberUtils.INTEGER_ZERO);
            if (!StringUtils.isNotBlank(param.getName()))
                flag = NumberUtils.INTEGER_ZERO;
        } else if (invoiceType > InvTypeEnum.PASS.getCode())
            flag = NumberUtils.INTEGER_ZERO;
        else if (oriList.isEmpty())
            flag = NumberUtils.INTEGER_ZERO;

        return flag;
    }

    /**
     * 生成凭证修改发票明细
     *
     * @param invId           发票id
     * @param detailListBy2DB 发票明细列表
     */
    private void updByCert(Long invId, List<InvoiceDetail> detailListBy2DB) {
        //查询发票明细列表
        List<InvoiceDetail> outList = detailORM.lambdaQuery()
                .eq(InvoiceDetail::getInvoiceId, invId)
                .list();

        //发票明细和发票明细id的映射
        Map<Long, InvoiceDetail> detailById = outList.stream().collect(Collectors.toMap(InvoiceDetail::getId, d -> d));

        //更新发票明细实例域
        detailListBy2DB.forEach(d -> {
            InvoiceDetail detail = detailById.get(d.getId());
            detail.setName(d.getName());
            detail.setAbstAuxId(d.getAbstAuxId());
        });

        //更新完成入库
        detailListBy2DB.clear();
        detailListBy2DB.addAll(detailById.values());
        detailORM.saveOrUpdateBatch(detailListBy2DB);
    }

    /**
     * 更新发票明细辅助核算
     *
     * @param detailList 发票明细列表
     */
    @PostMapping("/invoice/detail/aux")
    public Result<Boolean> updateDetailAux(@RequestBody List<DetailParam> detailList) {
        List<Long> detailIdList = new ArrayList<>();
        for (DetailParam param : detailList) {
            if (!ObjectUtils.isEmpty(param.getId()))
                detailIdList.add(param.getId());
        }
        if (detailIdList.isEmpty())
            return Result.success();

        List<InvoiceDetail> tmpDetailList = detailORM.listByIds(detailIdList);

        //发票明细和发票明细id的映射
        Map<Long, InvoiceDetail> detailById = tmpDetailList.stream().collect(Collectors.toMap(InvoiceDetail::getId, d -> d));

        //初始化辅助核算
        List<InvoiceDetail> detailListBy2DB = new ArrayList<>();
        for (DetailParam param : detailList) {
            if (!ObjectUtils.isEmpty(param.getId())) {
                InvoiceDetail detail = detailById.get(param.getId());
                detail.setAbstAuxId(param.getAbstAuxId());
                detail.setName(param.getName());
                detailListBy2DB.add(detail);
            }
        }
        if (detailListBy2DB.isEmpty())
            return Result.success();

        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            detailORM.updateBatchById(detailListBy2DB);

            transactionManager.commit(transaction);
            return Result.success();
        } catch (Exception e) {
            transactionManager.rollback(transaction);
            throw new RuntimeException(e);
        }
    }
}