package com.bbs.auth.app.company;

import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import com.bbs.Result;
import com.bbs.auth.entity.CompanyStructure;
import com.bbs.auth.service.CompanyService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import java.util.List;

import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ZERO;
import static org.apache.commons.lang3.math.NumberUtils.LONG_ZERO;

@RestController
@RequestMapping
public class SearchStructure {

    @Resource
    private CompanyService companyService;


    @GetMapping("/company/structure")
    @Cacheable("companyStructure")
    public Result<VO> search(@RequestParam(name = "id") Long id) {
        List<CompanyStructure> companyStructureList = companyService.searchStructure(id);
        if(companyStructureList.size() == INTEGER_ZERO) companyStructureList = companyService.searchStructure();
        TreeNodeConfig treeNodeConfig = new TreeNodeConfig();
        treeNodeConfig.setDeep(5);
        treeNodeConfig.setParentIdKey("pid");
        List<Tree<Long>> companyStructureTree = TreeUtil.build(companyStructureList, LONG_ZERO, treeNodeConfig,
                (companyStructure, tree) -> {
                    tree.setId(companyStructure.getId());
                    tree.setParentId(companyStructure.getPid());
                    tree.setWeight(companyStructure.getWeight());
                    tree.setName(companyStructure.getName());
                    // 扩展属性 ...
                    tree.putExtra("companyId", companyStructure.getCompanyId());
                    tree.putExtra("label", companyStructure.getName());
                    tree.putExtra("introduction", companyStructure.getIntroduction());
                    tree.putExtra("createTime", companyStructure.getCreateTime());
                    tree.putExtra("createBy", companyStructure.getCreateBy());
                    tree.putExtra("updateTime", companyStructure.getUpdateTime());
                    tree.putExtra("updateBy", companyStructure.getUpdateBy());
        });
        Boolean isSetCompanyStructure = companyService.searchIsSetCompanyStructure(id);
        return Result.success(new VO(isSetCompanyStructure, companyStructureTree));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VO {

        Boolean isSetCompanyStructure;

        List<Tree<Long>> companyStructure;
    }
}
