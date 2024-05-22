package com.bbs.financial.api.certificate.search;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bbs.Result;
import com.bbs.financial.entity.Certificate;
import com.bbs.financial.service.CertificateService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RequestMapping
@RestController
public class SearchCertificate {

    @Resource
    private CertificateService certificateService;

    @GetMapping("/certificate")
    public Result<Certificate> search(@RequestParam Long id) {
        return Result.success(
                certificateService.getOneDeep(Wrappers.<Certificate>lambdaQuery().eq(Certificate::getId, id), conf -> conf.loop(true))
        );
    }
}
