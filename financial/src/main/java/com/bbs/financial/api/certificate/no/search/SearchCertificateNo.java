package com.bbs.financial.api.certificate.no.search;

import cn.hutool.core.date.DateUtil;
import com.bbs.Result;
import com.bbs.financial.entity.Certificate;
import com.bbs.financial.service.CertificateService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;

import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ONE;

@RestController
@RequestMapping
public class SearchCertificateNo {

    @Resource
    private CertificateService db;

    @GetMapping("/certificate/no")
    public Result<Long> search(Param param) {
        Date date = new Date(param.getDate());
        return Result.success(db.lambdaQuery().eq(Certificate::getCompanyId, param.getCompanyId())
                .ge(Certificate::getCreateTime, DateUtil.beginOfMonth(date))
                .lt(Certificate::getCreateTime, DateUtil.beginOfMonth(DateUtil.offsetMonth(date, INTEGER_ONE)))
                .count() + INTEGER_ONE);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Param {

        private Long date;

        private Long companyId;
    }
}
