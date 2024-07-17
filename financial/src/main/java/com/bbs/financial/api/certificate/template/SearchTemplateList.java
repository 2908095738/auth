package com.bbs.financial.api.certificate.template;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bbs.Result;
import com.bbs.financial.entity.CertificateTemplate;
import com.bbs.financial.service.CertificateTemplateService;
import com.bbs.financial.util.LoginUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping
@RestController
public class SearchTemplateList {

    @Resource
    private CertificateTemplateService db;

    @GetMapping("/certificate/template/list")
    public Result<List<CertificateTemplate>> search() {
        Long loginSetId = LoginUser.getLoginSetId();
        return Result.success(
                db.list(Wrappers.<CertificateTemplate>lambdaQuery().eq(CertificateTemplate::getAccountingSetId, loginSetId))
        );
    }
}
