package com.bbs.auth.app.company.staff;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.auth.entity.Company;
import com.bbs.auth.entity.User;
import com.bbs.auth.entity.UserCompany;
import com.bbs.auth.service.CompanyService;
import com.bbs.auth.service.UserCompanyService;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static java.util.Objects.nonNull;

@RestController("searchStaff")
@RequestMapping
public class Search {

    @Resource
    private CompanyService companyService;
    @Resource
    private UserCompanyService userCompanyService;

    @GetMapping("/company/staff")
    public Result<Company> search(
            @RequestParam("id") Long id,
            @RequestParam("current") Integer current,
            @RequestParam("size") Integer size
    ) {
        return Result.success(companyService.searchCompanyStaff(id, new Page<>(current, size)));
    }

    @GetMapping("/company/staff/list")
    public Result<Object> searchList(
            @RequestParam(required = false) String val,
            @RequestParam Long companyId,
            @RequestParam(value = "current", required = false) Integer current,
            @RequestParam(value = "size", required = false) Integer size
    ) {
        if(nonNull(current) && nonNull(size)) {
            return Result.success(userCompanyService.selectJoinListPage(
                    new Page<>(current, size),
                    UserCompany.class,
                    new MPJLambdaWrapper<UserCompany>()
                            .selectAll(UserCompany.class)
                            .leftJoin(User.class, User::getId, UserCompany::getUserId, ext -> ext
                                    .selectAssociation(User.class, UserCompany::getUser)
                            )
                            .eq(UserCompany::getCompanyId, companyId)
                            .and(StringUtils.isNotBlank(val), wrapper -> wrapper
                                    .like(User::getName, val)
                                    .or()
                                    .like(User::getPhone, val)
                            )
            ));
        } else {
            return Result.success(userCompanyService.selectJoinList(
                    UserCompany.class,
                    new MPJLambdaWrapper<UserCompany>()
                            .selectAll(UserCompany.class)
                            .leftJoin(User.class, User::getId, UserCompany::getUserId, ext -> ext
                                    .selectAssociation(User.class, UserCompany::getUser)
                            )
                            .eq(UserCompany::getCompanyId, companyId)
                            .and(StringUtils.isNotBlank(val), wrapper -> wrapper
                                    .like(User::getName, val)
                                    .or()
                                    .like(User::getPhone, val)
                            )
            ));
        }
    }

    /**
     * 查询非公司员工
     */
//    @GetMapping("/company/user/list")
//    public Result<Object> searchNonEmployeeList(
//            @RequestParam(required = false) String val,
//            @RequestParam Long companyId
//    ) {
//        if(nonNull(current) && nonNull(size)) {
//            return Result.success(userCompanyService.selectJoinListPage(
//                    new Page<>(current, size),
//                    UserCompany.class,
//                    new MPJLambdaWrapper<UserCompany>()
//                            .selectAll(UserCompany.class)
//                            .leftJoin(User.class, User::getId, UserCompany::getUserId, ext -> ext
//                                    .selectAssociation(User.class, UserCompany::getUser)
//                            )
//                            .eq(UserCompany::getCompanyId, companyId)
//                            .and(StringUtils.isNotBlank(val), wrapper -> wrapper
//                                    .like(User::getName, val)
//                                    .or()
//                                    .like(User::getPhone, val)
//                            )
//            ));
//        } else {
//            return Result.success(userCompanyService.selectJoinList(
//                    UserCompany.class,
//                    new MPJLambdaWrapper<UserCompany>()
//                            .selectAll(UserCompany.class)
//                            .leftJoin(User.class, User::getId, UserCompany::getUserId, ext -> ext
//                                    .selectAssociation(User.class, UserCompany::getUser)
//                            )
//                            .eq(UserCompany::getCompanyId, companyId)
//                            .and(StringUtils.isNotBlank(val), wrapper -> wrapper
//                                    .like(User::getName, val)
//                                    .or()
//                                    .like(User::getPhone, val)
//                            )
//            ));
//        }
//    }
}
