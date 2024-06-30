package com.bbs.financial.api.accountBook;

import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.financial.entity.CertificateAbstract;
import com.bbs.financial.service.AccountService;
import com.bbs.financial.service.CertificateAbstractService;
import com.bbs.financial.util.LoginUser;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class SearchAccountTree {

    @Resource
    private CertificateAbstractService certificateAbstractService;

    @Resource
    private AccountService accountService;

    @GetMapping("/certificate/accountTree")
    public Result<List<Tree<Long>>> accountTree(@RequestParam("createTime") String certificateCreateTime) {
        List<Tree<Long>> list = accountService.selectTree(LoginUser.getCompanyId(), certificateCreateTime);

        return Result.success(list);
    }

    @GetMapping("/certificate/account/abstract")
    public Result<Page<CertificateAbstract>> accountAbstract(@RequestParam("createTime") String certificateCreateTime,
                                                             @RequestParam("accountId")Long accountId,
                                                             @RequestParam("current")Integer current,
                                                             @RequestParam("size")Integer size) {
        Page<CertificateAbstract> list = certificateAbstractService.selectPage(LoginUser.getCompanyId(), certificateCreateTime, accountId, current, size);
        certificateAbstractService.initData(list);
        return Result.success(list);
    }


    @GetMapping("/certificate/account/general")
    public Result<Page<CertificateAbstract>> accountAbstract(@RequestParam("createTime") String certificateCreateTime,
                                                             @RequestParam("current")Integer current,
                                                             @RequestParam("size")Integer size) {
        Page<CertificateAbstract> list = certificateAbstractService.selectPage(LoginUser.getCompanyId(), certificateCreateTime,null, current, size);
        certificateAbstractService.initData(list);
        List<String> emnu = Lists.newArrayList("期初余额", "本期合计", "本年累计");
        list.setRecords(list.getRecords().stream().filter(item -> emnu.contains(item.getCertificateAbstract())).collect(Collectors.toList()));
        return Result.success(list);
    }





}
