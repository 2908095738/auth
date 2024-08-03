package com.clinic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bbs.util.FisrtWordsSqlUtils;
import com.bbs.util.MyStringUtil;
import com.clinic.dto.param.DrugParam;
import com.clinic.entity.Drug;
import com.clinic.mapper.DrugMapper;
import com.clinic.service.DrugService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.Objects.nonNull;

/**
 *
 */
@Service
public class DrugServiceImpl extends ServiceImpl<DrugMapper, Drug>
    implements DrugService {

    @Override
    public List<Drug> selectInfoDistinct() {
        return null;
    }

    @Override
    public Page<Drug> search(DrugParam param) {
        Long id = param.getId();
        LambdaQueryChainWrapper<Drug> wrapper = lambdaQuery();
        if(nonNull(id)) {
            wrapper = wrapper.eq(Drug::getId, id);
        } else {
            String name = param.getName();
            String manufacturer = param.getManufacturer();
            String drugNo = param.getDrugNo();
            String approvalNumber = param.getApprovalNumber();
            wrapper
                    .like(StringUtils.isNotBlank(manufacturer), Drug::getManufacturer, manufacturer)
                    .likeRight(StringUtils.isNotBlank(drugNo), Drug::getDrugNo, drugNo)
                    .like(StringUtils.isNotBlank(approvalNumber), Drug::getApprovalNumber, approvalNumber);
                    if(StringUtils.isNotBlank(name)){
                        if(!MyStringUtil.isContainChinese(name)){
                            String sql = FisrtWordsSqlUtils.getSql(name);
                            wrapper.apply(sql);
                        }else{
                            wrapper.like(Drug::getName,name);
                        }
                    }
        }
        return wrapper.page(param.toPage());
    }

    @Override
    public List<Drug> search(String val) {
        return lambdaQuery()
                .like(Drug::getName, val)
                .or()
                .like(Drug::getManufacturer, val)
                .or()
                .like(Drug::getDrugNo, val)
                .or()
                .like(Drug::getApprovalNumber, val)
                .or()
                .like(Drug::getRemark, val)
                .list();
    }

    @Override
    public Page<Drug> search(String name,  Page<Drug> tPage) {
        QueryWrapper<Drug> queryWrapper = new QueryWrapper<>();
        if(StringUtils.isNotBlank(name)) {
            if (name.matches("[a-zA-Z]+")) {
                // 如果是纯英文，进行拼音或首字母模糊匹配
                queryWrapper.apply("LOWER(CONVERT(name USING gbk)) LIKE LOWER(CONVERT({0} USING gbk)) OR LOWER(name) LIKE LOWER({0})", "%" + name + "%");
            } else {
                // 否则进行普通 LIKE 查询
                queryWrapper.like("name", name);
            }
        }

        return page(tPage, queryWrapper);
    }
}




