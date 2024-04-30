package com.bbs.auth.app.company;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.auth.entity.Company;
import com.bbs.auth.service.CompanyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping
public class SearchStaff {

    @Resource
    private CompanyService companyService;

    @GetMapping("/company/staff")
    public Result<Company> search(
            @RequestParam("id") Long id,
            @RequestParam("current") Integer current,
            @RequestParam("size") Integer size
    ) {
        return Result.success(companyService.searchCompanyStaff(id, new Page<>(current, size)));
    }
}
