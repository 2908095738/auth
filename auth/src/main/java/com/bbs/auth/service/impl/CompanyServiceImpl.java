package com.bbs.auth.service.impl;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.auth.converter.UserConverter;
import com.bbs.auth.entity.Company;
import com.bbs.auth.entity.CompanyStructure;
import com.bbs.auth.entity.UserCompany;
import com.bbs.auth.service.CompanyService;
import com.bbs.auth.mapper.CompanyMapper;
import com.bbs.auth.service.CompanyStructureService;
import com.bbs.auth.service.UserCompanyService;
import com.bbs.auth.service.UserService;
import com.bbs.auth.util.RedisUtil;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.bbs.auth.enums.RedisKeys.COMPANY_STRUCTURE;
import static com.bbs.auth.enums.RedisKeys.USER_COMPANY;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ZERO;
import static org.apache.commons.lang3.math.NumberUtils.LONG_ZERO;

/**
* @author 路晨霖
* @description 针对表【company(公司)】的数据库操作Service实现
* @createDate 2024-04-28 11:36:25
*/
@Service
public class CompanyServiceImpl extends MPJBaseServiceImpl<CompanyMapper, Company>
    implements CompanyService{

    @Resource
    private CompanyStructureService companyStructureService;

    @Resource
    private UserCompanyService userCompanyService;

    @Resource
    private UserService userService;

    @Resource
    private UserConverter userConverter;

    @Resource
    private RedisUtil redisUtil;
    @Resource
    private TransactionDefinition transactionDefinition;
    @Resource
    private DataSourceTransactionManager transactionManager;

    @Override
    public Boolean exists(Company company) {
        return lambdaQuery()
                .eq(StringUtils.isNotBlank(company.getName()), Company::getName, company.getName())
                .or()
                .eq(StringUtils.isNotBlank(company.getCode()), Company::getCode, company.getCode())
                .exists();
    }

    @Override
    public Boolean notExists(Company company) {
        return !exists(company);
    }

    @Override
    public List<CompanyStructure> searchStructure(Long companyID) {
        return searchStructure(companyID, null);
    }

    @Override
    public List<CompanyStructure> searchStructure() {
        return searchStructure(LONG_ZERO);
    }

    @Override
    public List<CompanyStructure> searchStructure(Long companyID, String name) {
        return companyStructureService.lambdaQuery()
                .eq(CompanyStructure::getCompanyId, companyID)
                .like(StringUtils.isNotBlank(name), CompanyStructure::getName, name)
                .list();
    }

    @Override
    public Company searchCompanyStaff(Long companyID, Page<UserCompany> page) {
        Company company = getById(companyID);
        Page<UserCompany> userCompanyPage = userCompanyService.lambdaQuery()
                .eq(UserCompany::getCompanyId, companyID)
                .page(page);
        fillUserToUserCompanyPage(userCompanyPage); //填充用户信息
        company.setStaffList(userCompanyPage);
        return company;
    }

    private void fillUserToUserCompanyPage(Page<UserCompany> userCompanyPage) {
        List<UserCompany> userCompanyList = userCompanyPage.getRecords();
        // 创建下标映射（用于快速填充用户信息）
        if(userCompanyList.size() > INTEGER_ZERO) {
            Set<Long> ids = new HashSet<>();
            Map<Long, Integer> indexMap = new HashMap<>();

            List<String> structureCacheKeys = new ArrayList<>();
            List<Long> structureIds = new ArrayList<>();
            Map<Long, Integer> structureIdAndIndexMap = new HashMap<>();
            for (int index = INTEGER_ZERO; index < userCompanyList.size(); index++) {
                UserCompany userCompany = userCompanyList.get(index);
                Long uid = userCompany.getUserId();
                indexMap.put(uid, index);
                ids.add(uid);

                Long structureId = userCompany.getStructureId();
                structureIdAndIndexMap.put(structureId, index);
                structureCacheKeys.add(COMPANY_STRUCTURE.key(structureId));

                structureIds.add(structureId);
            }
            List<String> structureStrList = redisUtil.multiGet(structureCacheKeys);
            List<Long> cacheEmptyIds = new ArrayList<>();
            Map<Long, Integer> cacheEmptyIdAndIndexMap = new HashMap<>();
            for (int index = 0; index < structureStrList.size(); index++) {
                String structureStr = structureStrList.get(index);
                if(StringUtils.isNotBlank(structureStr)) {
                    CompanyStructure companyStructure = JSONUtil.toBean(structureStr, CompanyStructure.class);
                    Integer userCompanyListIndex = structureIdAndIndexMap.get(companyStructure.getId());
                    UserCompany userCompany = userCompanyList.get(userCompanyListIndex);
                    userCompany.setStructure(companyStructure);
                } else {
                    Long emptyStructureID = structureIds.get(index);
                    cacheEmptyIds.add(emptyStructureID);
                    cacheEmptyIdAndIndexMap.put(emptyStructureID, index);
                }
            }
            if(cacheEmptyIds.size() > INTEGER_ZERO) {
                Map<String, String> cacheEmptyStructureCache = new HashMap<>();
                companyStructureService.listByIds(cacheEmptyIds).forEach(structure -> {
                    Long cacheEmptyStructureId = structure.getId();
                    cacheEmptyStructureCache.put(COMPANY_STRUCTURE.key(cacheEmptyStructureId), JSONUtil.toJsonPrettyStr(structure));
                    Integer cacheEmptyStructureIndex = cacheEmptyIdAndIndexMap.get(cacheEmptyStructureId);
                    userCompanyList.get(cacheEmptyStructureIndex).setStructure(structure);
                });
                redisUtil.multiSet(cacheEmptyStructureCache);
            }
            // 填充用户信息
            userService.search(ids).forEach(user -> {
                Integer index = indexMap.get(user.getId());
                UserCompany userCompany = userCompanyList.get(index);
                userCompany.setUser(userConverter.toVO(user));
            });
        }
    }

    @Override
    public List<UserCompany> searchCompany(Long uid) {
        String key = USER_COMPANY.key(uid);
        String str = redisUtil.get(key);
        List<UserCompany> companies;
        if(isNull(str)) {
            companies = userCompanyService.selectJoinList(UserCompany.class, new MPJLambdaWrapper<UserCompany>()
                    .selectAssociation(Company.class, UserCompany::getCompany)
                    .leftJoin(Company.class, Company::getId, UserCompany::getCompanyId)
                    .eq(UserCompany::getUserId, uid)
            );
            redisUtil.set(USER_COMPANY.key(uid), JSONUtil.toJsonPrettyStr(companies), 7, TimeUnit.DAYS);
        } else {
            companies = JSONUtil.toBean(str, new TypeReference<List<UserCompany>>() {}, true);
        }
        return companies;
    }

    @Override
    public void searchUserPosition(Long uid, Long companyID) {
         userCompanyService.lambdaQuery()
                 .eq(UserCompany::getCompanyId, companyID)
                 .eq(UserCompany::getUserId, uid)
                 .list();
    }

    @Override
    public Boolean searchIsSetCompanyStructure(Long companyID) {
        return companyStructureService.lambdaQuery()
                .eq(CompanyStructure::getCompanyId, companyID)
                .exists();
    }

    @Override
    public void createCompanyStructure(CompanyStructure companyStructure) {
        companyStructureService.save(companyStructure);
    }

    @Override
    public void join(Long userID, Long companyID, Long structureID, Boolean isAdmin) {
        Long loginUID = userService.loginUser().getId();
        UserCompany userCompany = new UserCompany(userID, companyID, structureID, loginUID);
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            userCompanyService.save(userCompany);
            if(nonNull(isAdmin) && isAdmin) {
                companyStructureService.lambdaUpdate()
                        .eq(CompanyStructure::getCompanyId, companyID)
                        .set(CompanyStructure::getAdmin, userID)
                        .update();
            }
            transactionManager.commit(transaction);
        } catch (Exception e) {
            transactionManager.rollback(transaction);
            throw new RuntimeException(e);
        }
    }
}




