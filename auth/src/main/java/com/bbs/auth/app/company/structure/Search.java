package com.bbs.auth.app.company.structure;

import com.bbs.Result;
import com.bbs.auth.entity.CompanyStructure;
import com.bbs.auth.service.CompanyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController("searchStructure")
@RequestMapping
public class Search {


    @Resource
    private CompanyService companyService;


    @GetMapping("/company/structure/all")
    public Result<List<CompanyStructure>> search(
            @RequestParam("id") Long id,
            @RequestParam(name = "name", required = false) String name
    ) {
        return Result.success(companyService.searchStructure(id, name));
    }
}
