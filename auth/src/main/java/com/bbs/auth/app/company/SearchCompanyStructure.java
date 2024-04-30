package com.bbs.auth.app.company;

import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import com.bbs.Result;
import com.bbs.auth.entity.CompanyStructure;
import com.bbs.auth.service.CompanyStructureService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.websocket.server.PathParam;

import java.util.List;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ZERO;
import static org.apache.commons.lang3.math.NumberUtils.LONG_ZERO;

@RestController
@RequestMapping
public class SearchCompanyStructure {

    @Resource
    private CompanyStructureService companyStructureService;

    @GetMapping("/company/structure")
    @Cacheable("companyStructure")
    public Result<List<Tree<Long>>> search(@PathParam("id") Long id) {
        List<CompanyStructure> companyStructureList;
        if(isNull(id)) {
            companyStructureList = companyStructureService.lambdaQuery()
                    .eq(CompanyStructure::getCompanyId, INTEGER_ZERO)
                    .list();
        } else {
            companyStructureList = companyStructureService.lambdaQuery()
                    .eq(CompanyStructure::getCompanyId, id)
                    .list();
        }
        TreeNodeConfig treeNodeConfig = new TreeNodeConfig();
        treeNodeConfig.setDeep(5);
        treeNodeConfig.setParentIdKey("pid");
        return Result.success(
                TreeUtil.build(companyStructureList, LONG_ZERO, treeNodeConfig,
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
                        })
        );
    }
}
