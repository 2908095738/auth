package com.bbs.financial.api.certificate.search;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.api.auth.User;
import com.bbs.api.auth.UserAPI;
import com.bbs.financial.entity.Certificate;
import com.bbs.financial.service.CertificateService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ONE;

@RequestMapping
@RestController
public class SearchCertificate {

    @Resource
    private CertificateService certificateService;

    @DubboReference
    private UserAPI userAPI;

    @GetMapping("/certificate")
    public Result<Certificate> search(@RequestParam Long id) {
        return Result.success(
                certificateService.getOneDeep(Wrappers.<Certificate>lambdaQuery().eq(Certificate::getId, id), conf -> conf.loop(true))
        );
    }

    @GetMapping("/certificate/list")
    public Result<Page<Certificate>> search(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam Long companyId,
            @RequestParam(name = "date", required = false) String dateStr
    ) {
        Date date = nonNull(dateStr) ? new Date(Long.parseLong(dateStr)) : new Date();
        Page<Certificate> certificatePage = certificateService.pageDeep(
                new Page<>(current, size),
                Wrappers.<Certificate>lambdaQuery()
                        .eq(Certificate::getCompanyId, companyId)
                        .ge(Certificate::getDate, DateUtil.beginOfMonth(date))
                        .lt(Certificate::getDate, DateUtil.beginOfMonth(DateUtil.offsetMonth(date, INTEGER_ONE)))
                , conf -> conf.loop(true)
        );
        // 获取【创建用户】&&【审核用户】的 userId Set
        Set<Long> userIds = filterUserIds(certificatePage);
        // 查询用户信息
        Map<Long, User> map = searchIdUserMap(userIds);
        // 回填用户信息
        fillUser(certificatePage, map);

        return Result.success(certificatePage);
    }

    private Set<Long> filterUserIds(Page<Certificate> certificatePage) {
        Set<Long> userIds = new HashSet<>();
        certificatePage.getRecords().forEach(certificate -> {
            userIds.add(certificate.getCreateBy());
            Long authUserId = certificate.getAuthBy();
            if(nonNull(authUserId)) userIds.add(authUserId);
        });
        return userIds;
    }

    private Map<Long, User> searchIdUserMap(Set<Long> userIds) {
        return userAPI.getUserList(userIds).stream().collect(Collectors.toMap(User::getId, user -> user));
    }

    private void fillUser(Page<Certificate> certificatePage, Map<Long, User> map) {
        certificatePage.getRecords().forEach(certificate -> {
            certificate.setCreateUser(map.get(certificate.getCreateBy()));
            Long authUserId = certificate.getAuthBy();
            if(nonNull(authUserId)) certificate.setAuthUser(map.get(certificate.getAuthBy()));
        });
    }
}
