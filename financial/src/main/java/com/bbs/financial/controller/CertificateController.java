package com.bbs.financial.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.api.auth.UserAPI;
import com.bbs.api.auth.company.CompanyAPI;
import com.bbs.financial.entity.Account;
import com.bbs.financial.entity.Certificate;
import com.bbs.financial.entity.CertificateAbstract;
import com.bbs.financial.service.CertificateAbstractService;
import com.bbs.financial.service.CertificateService;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

import static com.bbs.Result.success;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ONE;

/**
 * 记账凭证Controller
 * @author vctgo
 * @date 2024-05-13
 */
@RestController
@RequestMapping
public class CertificateController {

    @Resource
    private CertificateService certificateService;

    @DubboReference
    private CompanyAPI companyAPI;

    @DubboReference
    private UserAPI userAPI;
    @Resource
    private TransactionDefinition transactionDefinition;
    @Resource
    private DataSourceTransactionManager transactionManager;

    /**
     * 查询记账凭证列表
     */
//    @GetMapping("/certificate/list")
    public Result<Page<Certificate>> list(@RequestParam Integer current, @RequestParam Integer size) {
        Page<Certificate> page = certificateService.page(new Page<>(current, size), new MPJLambdaWrapper<Certificate>()
                .selectAll(Certificate.class)
                .leftJoin(CertificateAbstract.class, CertificateAbstract::getCertificateId, Certificate::getId, ext -> ext
                        .selectCollection(CertificateAbstract.class, Certificate::getAbstracts)
                )
        );
        fillCompany(page);

        return success(page);
    }

    private void fillCompany(Page<Certificate> page) {
        List<Certificate> certificates = page.getRecords();

        Map<Long, List<Certificate>> companyIdAndCertificateMap = new HashMap<>();  // 存储公司 ID, 凭证列表的 map（用于查询到公司信息后，快速填充到每个凭证中）
        Set<Long> companyIds = new HashSet<>(); //存储公司 ID 的 set（用于查询公司信息）
        for (Certificate certificate : certificates) {
            Long accountingSetId = certificate.getAccountingSetId();

            // 将公司信息，以 Map.Entity<公司ID, List<凭证>> 的格式存储
            List<Certificate> certificateList = companyIdAndCertificateMap.getOrDefault(accountingSetId, new ArrayList<>());
            certificateList.add(certificate);
            companyIdAndCertificateMap.putIfAbsent(accountingSetId, certificateList);

            // 去重保存公司 ID
            companyIds.add(accountingSetId);
        }

        // 查询公司信息，并回填到每个凭证中
        companyAPI.list(companyIds).forEach(company -> companyIdAndCertificateMap.get(company.getId()).forEach(certificate -> certificate.setCompany(company)));
    }

    /**
     * 删除记账凭证
     */
    @DeleteMapping("/certificate/batch/{idList}")
    public Result<Boolean> remove(@PathVariable List<Long> idList)
    {
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            certificateService.removeBatchByIds(idList);
            certificateAbstractService.lambdaUpdate()
                    .in(CertificateAbstract::getCertificateId, idList)
                    .remove();
            transactionManager.commit(transaction);
            return Result.success();
        } catch (Exception e) {
            transactionManager.rollback(transaction);
            return Result.failed(e.getMessage());
        }
    }

    @Resource
    private CertificateAbstractService certificateAbstractService;

    @GetMapping("/certificate/abstract")
    public Result<Page<CertificateAbstract>> searchCertificateAbstract(@RequestParam Integer current, @RequestParam(defaultValue = "10") Integer size) {
        Page<CertificateAbstract> page = certificateAbstractService.selectJoinListPage(new Page<>(current, size), CertificateAbstract.class, new MPJLambdaWrapper<CertificateAbstract>()
                .selectAll(CertificateAbstract.class)
                .leftJoin(Account.class, Account::getId, CertificateAbstract::getAccountId, ext -> ext
                        .selectAssociation(Account.class, CertificateAbstract::getAccount)
                )
                .leftJoin(Certificate.class, Certificate::getId, CertificateAbstract::getCertificateId, ext -> ext
                        .selectAssociation(Certificate.class, CertificateAbstract::getCertificate)
                )
        );
        fillCreateUserAndAuthUser(page);
        return success(page);
    }

    private void fillCreateUserAndAuthUser(Page<CertificateAbstract> page) {
        if(page.getRecords().size() >= INTEGER_ONE) {
            Map<Long, List<Certificate>> needFillAuthUserMap = new HashMap<>();
            Map<Long, List<Certificate>> needFillCreateUserMap = new HashMap<>();
            List<CertificateAbstract> records = page.getRecords();
            Set<Long> uidList = new HashSet<>();
            for (CertificateAbstract record : records) {
                Certificate certificate = record.getCertificate();
                Long authUserID = certificate.getAuthBy();
                Long createByUserID = certificate.getCreateBy();

                if (nonNull(authUserID)) {
                    List<Certificate> authUserList = needFillAuthUserMap.getOrDefault(authUserID, new ArrayList<>());
                    authUserList.add(certificate);
                    needFillAuthUserMap.putIfAbsent(authUserID, authUserList);
                    uidList.add(authUserID);
                }

                if (nonNull(createByUserID)) {
                    List<Certificate> createUserList = needFillCreateUserMap.getOrDefault(createByUserID, new ArrayList<>());
                    createUserList.add(certificate);
                    needFillCreateUserMap.putIfAbsent(createByUserID, createUserList);
                    uidList.add(createByUserID);
                }
            }
            if(uidList.size() >= INTEGER_ONE) {
                userAPI.getUserList(uidList).forEach(user -> {
                    Long uid = user.getId();
                    needFillAuthUserMap.get(uid).forEach(certificate -> certificate.setAuthUser(user));
                    needFillCreateUserMap.get(uid).forEach(certificate -> certificate.setCreateUser(user));
                });
            }
        }
    }
}
