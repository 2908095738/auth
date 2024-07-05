package com.bbs.financial.api.accountBook;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.financial.entity.CertificateAbstract;
import com.bbs.financial.service.CertificateAbstractService;
import com.bbs.financial.util.LoginUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;


@RestController
public class ColumnarController {

    @Resource
    private CertificateAbstractService certificateAbstractService;
    @GetMapping("/certificate/account/columnar")
    public Result<List<CertificateAbstract>> columnarAccount(@RequestParam("createTime") String certificateCreateTime, @RequestParam("cAccountId")Long accountId) {
        Page<CertificateAbstract> list = certificateAbstractService.selectPage(LoginUser.getCompanyId(), certificateCreateTime, accountId, 1, 1000);
        certificateAbstractService.initDataByMonth(list);
        return Result.success(list.getRecords());
    }


}
