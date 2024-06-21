package com.bbs.financial.api.accountBook;

import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.financial.entity.CertificateAbstract;
import com.bbs.financial.service.AccountService;
import com.bbs.financial.service.CertificateAbstractService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@Slf4j
public class SearchAccountTree {

    @Resource
    private CertificateAbstractService certificateAbstractService;

    @Resource
    private AccountService accountService;

    @GetMapping("/certificate/accountTree")
    public Result<List<Tree<Long>>> accountTree(@RequestParam("companyId")Long companyId,
                                                @RequestParam("createTime") String certificateCreateTime) {
        List<Tree<Long>> list = accountService.selectTree(companyId, certificateCreateTime);

        return Result.success(list);
    }

    @GetMapping("/certificate/account/abstract")
    public Result<Page<CertificateAbstract>> accountAbstract(@RequestParam("companyId")Long companyId,
                                                             @RequestParam("createTime") String certificateCreateTime,
                                                             @RequestParam("accountId")Long accountId,
                                                             @RequestParam("current")Integer current,
                                                             @RequestParam("size")Integer size) {
        Page<CertificateAbstract> list = certificateAbstractService.selectPage(companyId, certificateCreateTime, accountId, current, size);

        return Result.success(list);
    }


    @GetMapping("/certificate/account/general")
    public Result<Page<CertificateAbstract>> accountAbstract(@RequestParam("companyId")Long companyId,
                                                             @RequestParam("createTime") String certificateCreateTime,
                                                             @RequestParam("current")Integer current,
                                                             @RequestParam("size")Integer size) {
        Page<CertificateAbstract> list = certificateAbstractService.selectPage(companyId, certificateCreateTime, current, size);

        return Result.success(list);
    }

}
