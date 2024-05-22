package com.bbs.auth.app.dubbo;

import com.bbs.api.auth.company.CompanyAPI;
import com.bbs.auth.converter.CompanyConverter;
import com.bbs.auth.service.CompanyService;
import com.bbs.vo.Company;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

@Slf4j
@Component
@DubboService
public class CompanyAPIImpl implements CompanyAPI {

    @Resource
    private CompanyService companyService;

    @Resource
    private CompanyConverter converter;

    @Override
    public List<Company> list(Collection<Long> ids) {
        return converter.toVO(companyService.listByIds(ids));
    }
}
