package com.bbs.financial.api.certificate.file;

import cn.hutool.core.date.DateUtil;
import com.bbs.Result;
import com.bbs.financial.entity.CertificateFile;
import com.bbs.financial.service.CertificateFileService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ONE;

@RestController
@RequestMapping
public class SearchFile {


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Param {
        /**
         * 公司ID
         */
        private Long companyId;
        /**
         * 凭证字
         */
        private String certificateWord;

        /**
         * 编号（凭证号）
         */
        private Long no;

        /**
         * 日期
         */
        private String date;
    }

    @Resource
    private CertificateFileService db;

    @GetMapping("/certificate/file")
    public Result<List<CertificateFile>> search(
            Param param
    ) {
        Date date = new Date(Long.parseLong(param.date));
        return Result.success(db.lambdaQuery()
                .eq(CertificateFile::getCompanyId, param.getCompanyId())
                .eq(CertificateFile::getCertificateWord, param.certificateWord)
                .eq(CertificateFile::getNo, param.no)
                .ge(CertificateFile::getDate, DateUtil.beginOfMonth(date))
                .lt(CertificateFile::getDate, DateUtil.beginOfMonth(DateUtil.offsetMonth(date, INTEGER_ONE)))
                .list()
        );
    }
}
