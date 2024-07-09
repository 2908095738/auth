package com.bbs.financial.api.certificate.search;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import com.bbs.financial.util.LoginUser;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
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
import static org.apache.commons.lang3.StringUtils.isNotBlank;
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
        Certificate certificate = certificateService.selectJoinOne(Certificate.class, new MPJLambdaWrapper<Certificate>()
                .selectAll(Certificate.class)
                .selectCollection(CertificateAbstract.class, Certificate::getAbstracts, ext -> ext
                        .association(Account.class, CertificateAbstract::getAccount)
                )
                .selectCollection(CertificateFile.class, Certificate::getFiles)

                // left join 凭证科目表
                .leftJoin(CertificateAbstract.class, CertificateAbstract::getCertificateId, Certificate::getId)
                // left join 科目表
                .leftJoin(Account.class, Account::getId, CertificateAbstract::getAccountId)
                // left join 附件表
                .leftJoin(CertificateFile.class, CertificateFile::getCertificateId, Certificate::getId)

                .eq(Certificate::getId, id)
        );
        if(nonNull(certificate)) {
            certificate.setCreateUser(userAPI.getUserList(Collections.singletonList(certificate.getCreateBy())).get(INTEGER_ZERO));
        }
        return Result.success(certificate);
    }

    @GetMapping("/certificate/list")
    public Result<Page<Certificate>> search(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(name = "words", required = false) List<String> words,
            @RequestParam(name = "no", required = false) Long no,
            @RequestParam(name = "createUserIds", required = false) List<Long> createUserIds,
            @RequestParam(name = "authUserIds", required = false) List<Long> authUserIds,
            @RequestParam(name = "startDate", required = false) Long startDateLong,
            @RequestParam(name = "endDate", required = false) Long endDateLong
    ) {
        Page<Certificate> certificatePage = certificateService.selectJoinListPage(new Page<>(current, size), Certificate.class, new MPJLambdaWrapper<Certificate>()
                .selectAll(Certificate.class)

                .selectCollection(CertificateAbstract.class, Certificate::getAbstracts, ext -> ext
                        .association(Account.class, CertificateAbstract::getAccount)
                )
                .selectCollection(CertificateFile.class, Certificate::getFiles)

                .leftJoin(CertificateAbstract.class, CertificateAbstract::getCertificateId, Certificate::getId)
                .leftJoin(Account.class, Account::getId, CertificateAbstract::getAccountId)
                .leftJoin(CertificateFile.class, CertificateFile::getCertificateId, Certificate::getId)

                .eq(Certificate::getCompanyId, LoginUser.getCompanyId())
                .eq(nonNull(no),Certificate::getNo,no)
                .in(nonNull(words) && words.size() > INTEGER_ZERO, Certificate::getCertificateWord, words)
                .in(nonNull(createUserIds) && createUserIds.size() > INTEGER_ZERO, Certificate::getCreateBy)
                .in(nonNull(authUserIds) && authUserIds.size() > INTEGER_ZERO, Certificate::getAuthBy)
                .and(nonNull(startDateLong), ext -> ext
                        .ge(Certificate::getDate, new Date(startDateLong))
                        .lt(nonNull(endDateLong), Certificate::getDate, new Date(endDateLong))
                )
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
     * @return 【凭证字】去重 List
     */
    @GetMapping("/certificate/word/list")
    public Result<List<CertificateWordEnum.Item>> searchCertificateWord(
            @RequestParam(required = false) String word
    ) {
        List<Object> wordObjs = certificateService.listObjs(new LambdaQueryWrapper<Certificate>()
                .select(Certificate::getCertificateWord)
                .eq(Certificate::getCompanyId, LoginUser.getCompanyId())
                .groupBy(Certificate::getCertificateWord)
        );

        Stream<CertificateWordEnum.Item> stream = wordObjs.stream()
                .map(obj -> new CertificateWordEnum.Item(CertificateWordEnum.enumMap.get(Integer.valueOf((String) obj))));
        if(isNotBlank(word)) {
            stream = stream.filter(item -> item.getMsg().indexOf(word) >= INTEGER_ZERO);
        }
        return Result.success(stream.collect(Collectors.toList()));
    }
}
