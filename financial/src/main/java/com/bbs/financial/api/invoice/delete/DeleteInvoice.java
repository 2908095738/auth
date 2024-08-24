package com.bbs.financial.api.invoice.delete;

import com.bbs.Result;
import com.bbs.financial.api.certificate.delete.DelCertificate;
import com.bbs.financial.entity.*;
import com.bbs.financial.enums.CertTypeEnum;
import com.bbs.financial.service.*;
import com.bbs.financial.util.ORMUtil;
import com.bbs.financial.util.SpringUtil;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

@RequestMapping
@RestController
public class DeleteInvoice {
    @Resource
    private TransactionDefinition transactionDefinition;
    @Resource
    private DataSourceTransactionManager transactionManager;

    @Resource
    private ApplicationContext appContext;

    @Resource
    private InvoiceService orm;

    @Resource
    private InvoiceDetailService detailORM;

    @DeleteMapping("/invoice/{idList}")
    public Result<Boolean> remove(@PathVariable List<Long> idList) {
        return ORMUtil.fastTran(() -> {
            orm.removeBatchByIds(idList);

            detailORM.lambdaUpdate()
                    .in(InvoiceDetail::getInvoiceId, idList)
                    .remove();
        }, transactionManager, transactionDefinition);
    }

    @DeleteMapping("/invoice/cert/{id}/{certId}")
    public Result<Boolean> removeCert(@PathVariable Long id, @PathVariable Long certId) {
        return ORMUtil.fastTran(() -> {
            SpringUtil.getRespData(DelCertificate.class, appContext,
                    c -> c.remove(Collections.singletonList(new DelCertificate.DelParam(CertTypeEnum.NONE.getType(), certId))));

            //修改发票
            orm.lambdaUpdate()
                    .set(Invoice::getCertificateId, null)
                    .set(Invoice::getTempName, null)
                    .eq(Invoice::getId, id)
                    .update();
        }, transactionManager, transactionDefinition);
    }

    /**
     * 清除发票凭证
     *
     * @param certIdList 凭证id列表
     */
    public Result<Boolean> clearCert(List<Long> certIdList) {
        return ORMUtil.fastTran(() -> Result.success(
                        orm.lambdaUpdate()
                                .set(Invoice::getCertificateId, null)
                                .in(Invoice::getId, getInvIdListByCert(certIdList))
                                .update()),
                transactionManager, transactionDefinition);
    }

    /**
     * 根据凭证id获取发票id列表
     *
     * @param certIdList 凭证id列表
     */
    private List<Long> getInvIdListByCert(List<Long> certIdList) {
        return orm.selectJoinList(Long.class, new MPJLambdaWrapper<Invoice>()
                .select(Invoice::getId)
                .in(Invoice::getCertificateId, certIdList));
    }
}