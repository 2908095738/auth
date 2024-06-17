package com.bbs.financial.api.accountBook;

import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.financial.entity.CertificateAbstract;
import com.bbs.financial.service.AccountService;
import com.bbs.financial.service.CertificateAbstractService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
public class SearchAccountTree {

    @Resource
    private CertificateAbstractService certificateAbstractService;

    @Resource
    private AccountService accountService;

    @GetMapping("/certificate/accountTree")
    public Result<List<Tree<Long>>> accountTree(Long companyId, String certificateCreateTime) {
        List<Tree<Long>> list = accountService.selectTree(companyId, certificateCreateTime);
        return Result.success(list);
    }

    @GetMapping("/certificate/abstract")
    public Result<Page<CertificateAbstract>> accountAbstractTree(Long companyId, String certificateCreateTime, Long accountId, Integer current, Integer size) {
        Page<CertificateAbstract> list = certificateAbstractService.selectPage(companyId, certificateCreateTime, accountId, current, size);





        return Result.success(list);
    }



}
