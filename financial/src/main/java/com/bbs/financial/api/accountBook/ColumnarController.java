package com.bbs.financial.api.accountBook;

import com.bbs.Result;
import com.bbs.financial.entity.CertificateAbstract;
import com.bbs.financial.service.CertificateAbstractService;
import com.bbs.financial.util.LoginUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;


@RestController
public class ColumnarController {

    @Resource
    private CertificateAbstractService certificateAbstractService;
    @GetMapping("/certificate/account/columnar")
    public Result<List<CertificateAbstract>> columnarAccount(@RequestParam("startCreateTime") Date certificateStartCreateTime,
                                                                 @RequestParam("endCreateTime") Date certificateEndCreateTime,
                                                             @RequestParam("cAccountId")Long accountId) {
        List<CertificateAbstract> list = certificateAbstractService.selectList(LoginUser.getLoginSetId(), certificateStartCreateTime,certificateEndCreateTime, accountId);
        certificateAbstractService.initDataByMonth(list);
        return Result.success(list);
    }


}
