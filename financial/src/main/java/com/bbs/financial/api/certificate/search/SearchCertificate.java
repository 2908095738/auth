package com.bbs.financial.api.certificate.search;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.api.auth.User;
import com.bbs.api.auth.UserAPI;
import com.bbs.enums.financial.CertificateWordEnum;
import com.bbs.financial.entity.Account;
import com.bbs.financial.entity.Certificate;
import com.bbs.financial.entity.CertificateAbstract;
import com.bbs.financial.entity.CertificateFile;
import com.bbs.financial.service.CertificateService;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ONE;
import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ZERO;

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
                certificateService.selectJoinOne(Certificate.class, new MPJLambdaWrapper<Certificate>()
                        .selectAll(Certificate.class)

                        // left join 凭证科目表
                        .leftJoin(CertificateAbstract.class, CertificateAbstract::getCertificateId, Certificate::getId, ext -> ext
                                .selectCollection(CertificateAbstract.class, Certificate::getAbstracts)

                                // left join 科目表
                                .leftJoin(Account.class, Account::getId, CertificateAbstract::getAccountId, ext2 -> ext2
                                        .selectAssociation(Account.class, CertificateAbstract::getAccount)
                                )
                        )

                        // left join 附件表
                        .leftJoin(CertificateFile.class, CertificateFile::getCertificateId, Certificate::getId, ext -> ext
                                .selectCollection(CertificateFile.class, Certificate::getFiles)
                        )

                        .eq(Certificate::getId, id)
                )
        );
    }

    @GetMapping("/certificate/list")
    public Result<Page<Certificate>> search(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam Long companyId,
            @RequestParam(name = "words", required = false) List<String> words,
            @RequestParam(name = "createUserIds", required = false) List<Long> createUserIds,
            @RequestParam(name = "authUserIds", required = false) List<Long> authUserIds,
            @RequestParam(name = "date", required = false) String dateStr
    ) {
        Date date = nonNull(dateStr) ? new Date(Long.parseLong(dateStr)) : new Date();
        Page<Certificate> certificatePage = certificateService.selectJoinListPage(new Page<>(current, size), Certificate.class, new MPJLambdaWrapper<Certificate>()
                .selectAll(Certificate.class)

                .selectCollection(CertificateAbstract.class, Certificate::getAbstracts, ext -> ext
                        .association(Account.class, CertificateAbstract::getAccount)
                )
                .selectCollection(CertificateFile.class, Certificate::getFiles)

                .leftJoin(CertificateAbstract.class, CertificateAbstract::getCertificateId, Certificate::getId)
                .leftJoin(Account.class, Account::getId, CertificateAbstract::getAccountId)
                .leftJoin(CertificateFile.class, CertificateFile::getCertificateId, Certificate::getId)

                .eq(Certificate::getCompanyId, companyId)
                .in(nonNull(words) && words.size() > INTEGER_ZERO, Certificate::getCertificateWord, words)
                .in(nonNull(createUserIds) && createUserIds.size() > INTEGER_ZERO, Certificate::getCreateBy)
                .in(nonNull(authUserIds) && authUserIds.size() > INTEGER_ZERO, Certificate::getAuthBy)
                .ge(Certificate::getDate, DateUtil.beginOfMonth(date))
                .lt(Certificate::getDate, DateUtil.beginOfMonth(DateUtil.offsetMonth(date, INTEGER_ONE)))
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

    /**
     * 【凭证字】去重
     * @param companyId 公司ID
     * @return 【凭证字】去重 List
     */
    @GetMapping("/certificate/word/list")
    public Result<List<CertificateWordEnum.Item>> searchCertificateWord(
            @RequestParam(required = false) String word,
            @RequestParam Long companyId
    ) {
        List<Object> wordObjs = certificateService.listObjs(new LambdaQueryWrapper<Certificate>()
                .select(Certificate::getCertificateWord)
                .eq(Certificate::getCompanyId, companyId)
                .groupBy(Certificate::getCertificateWord)
        );

        Stream<CertificateWordEnum.Item> stream = wordObjs.stream()
                .map(obj -> new CertificateWordEnum.Item(CertificateWordEnum.enumMap.get(Integer.valueOf((String) obj))));
        if(StringUtils.isNotBlank(word)) {
            stream = stream.filter(item -> item.getMsg().indexOf(word) >= INTEGER_ZERO);
        }
        return Result.success(stream.collect(Collectors.toList()));
    }

    @GetMapping("/certificate/create/list")
    public Result<List<User>> searchCreateName(
            @RequestParam(required = false) String createUserName,
            @RequestParam Long companyId
    ) {
        List<Long> createUserIds = certificateService.listObjs(new LambdaQueryWrapper<Certificate>()
                .select(Certificate::getCreateBy)
                .eq(Certificate::getCompanyId, companyId)
                .isNotNull(Certificate::getCreateBy)
                .groupBy(Certificate::getCreateBy)
        );

        List<User> userList = new ArrayList<>(createUserIds.size());
        if(createUserIds.size() > INTEGER_ZERO) {
            userList = userAPI.getUserList(createUserIds);
            if(StringUtils.isNotBlank(createUserName)) {
                userList = userList.stream()
                        .filter(user -> user.getName().indexOf(createUserName) >= INTEGER_ZERO)
                        .collect(Collectors.toList());
            }
        }
        return Result.success(userList);
    }

    @GetMapping("/certificate/auth/list")
    public Result<List<User>> searchAuthName(
            @RequestParam(required = false) String authUserName,
            @RequestParam Long companyId
    ) {
        List<Long> authUserIds = certificateService.listObjs(new LambdaQueryWrapper<Certificate>()
                .select(Certificate::getAuthBy)
                .eq(Certificate::getCompanyId, companyId)
                .isNotNull(Certificate::getAuthBy)
                .groupBy(Certificate::getAuthBy)
        );

        List<User> userList = new ArrayList<>(authUserIds.size());
        if(authUserIds.size() > INTEGER_ZERO) {
            userList = userAPI.getUserList(authUserIds);
            if(StringUtils.isNotBlank(authUserName)) {
                userList = userList.stream()
                        .filter(user -> user.getName().indexOf(authUserName) >= INTEGER_ZERO)
                        .collect(Collectors.toList());
            }
        }
        return Result.success(userList);
    }
}
