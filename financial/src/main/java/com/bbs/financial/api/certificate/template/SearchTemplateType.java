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
public class SearchTemplateType {

    @Resource
    private CertificateTemplateService db;

    @GetMapping("/certificate/template/type/list")
    public Result<List<String>> search() {
        return Result.success(db.listObjs(Wrappers.<CertificateTemplate>lambdaQuery()
                .select(CertificateTemplate::getType)
                .eq(CertificateTemplate::getCompanyId, LoginUser.getCompanyId())
                .groupBy(CertificateTemplate::getType)));
    }
}
