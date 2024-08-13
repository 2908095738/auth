package com.bbs.financial.api.invoice.search;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.financial.entity.*;
import com.bbs.financial.service.CertificateTemplateService;
import com.bbs.financial.service.InvoiceService;
import com.bbs.financial.util.LoginUser;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

@RestController
@RequestMapping
public class SearchTemplate {

    @Resource
    private InvoiceService invORM;

    @Resource
    private CertificateTemplateService certTempORM;

    /**
     * 获取凭证模板详情
     *
     * @param id 凭证模板id
     */
    @GetMapping("/invoice/template/{id}")
    public Result<CertificateTemplate> search(@PathVariable Long id) {
        return Result.success(invORM.listCertTemp(Collections.singletonList(id)).get(0));
    }

    /**
     * 查询凭证模板分页
     *
     * @param current         页码
     * @param size            条数
     * @param invoiceCategory 发票分类：0.销项发票;1.进项发票;2.费用小票;
     * @param isActive        是否启用该项目：true.启用;false.关闭;
     * @param name            模板名称
     */
    @GetMapping("/invoice/template/list/{current}/{size}/{invoiceCategory}/{isActive}/{name}")
    public Result<Page<CertificateTemplate>> searchPage(@PathVariable Integer current, @PathVariable Integer size,
                                                        @PathVariable Integer invoiceCategory, @PathVariable Boolean isActive,
                                                        @PathVariable(required = false) String name) {
        Page<CertificateTemplate> tempPage = invORM.searchTemp(current, size, invoiceCategory, isActive, name);
        if (tempPage.getRecords().isEmpty())
            return Result.success(new Page<>());

        //凭证模板摘要-辅助核算赋值

        resetTempList(tempPage);

        return Result.success(tempPage);
    }

    /**
     * 重置凭证模板列表
     *
     * @param tempPage 凭证模板分页
     */
    private void resetTempList(Page<CertificateTemplate> tempPage) {
        sortByTemp(tempPage);
        sortByTempAbstList(tempPage.getRecords());
    }

    /**
     * 凭证模板列表排序
     *
     * @param tempPage 凭证模板分页
     */
    private void sortByTemp(Page<CertificateTemplate> tempPage) {
        CertificateTemplate defaultTemp = tempPage.getRecords().stream().filter(t -> t.getIsDefault()).findFirst().orElse(null);
        if (Objects.nonNull(defaultTemp)) {
            List<CertificateTemplate> doneList = new ArrayList<>();
            doneList.add(defaultTemp);

            tempPage.getRecords().remove(defaultTemp);
            doneList.addAll(tempPage.getRecords());

            tempPage.setRecords(doneList);
        }
    }

    /**
     * 凭证模板摘要列表排序
     *
     * @param tempList 凭证模板列表
     */
    private void sortByTempAbstList(List<CertificateTemplate> tempList) {
        tempList.forEach(t -> t.getTemplateAbstractList().sort((l, r) -> {
            Long lId = l.getId();
            Long rId = r.getId();

            if (lId > rId)
                return 1;
            else if (lId < rId)
                return -1;
            else
                return 0;
        }));
    }

    /**
     * 获取默认发票凭证模板
     *
     * @param invoiceCate 发票分类：0.销项发票;1.进项发票;2.费用小票;
     */
    @GetMapping("/defaultTemplate/{invoiceCate}")
    public Result<CertificateTemplate> getDefault(@PathVariable Integer invoiceCate) {
        return Result.success(certTempORM.selectJoinOne(CertificateTemplate.class,
                new MPJLambdaWrapper<CertificateTemplate>()
                        .selectCollection(CertificateTemplateAbstract.class, CertificateTemplate::getTemplateAbstractList, ext ->
                                ext.association(Account.class, CertificateTemplateAbstract::getAccount)
                        )
                        .leftJoin(CertificateTemplateAbstract.class, CertificateTemplateAbstract::getTemplateId, CertificateTemplate::getId)
                        .leftJoin(Account.class, Account::getId, CertificateTemplateAbstract::getAccountId)

                        .eq(CertificateTemplate::getAccountingSetId, LoginUser.getLoginSetId())
                        .eq(CertificateTemplate::getInvoiceCategory, invoiceCate)
                        .eq(CertificateTemplate::getIsDefault, Boolean.TRUE)
                        .eq(CertificateTemplate::getIsActive, Boolean.TRUE)

                        .orderByAsc(CertificateTemplateAbstract::getId)
        ));
    }
}