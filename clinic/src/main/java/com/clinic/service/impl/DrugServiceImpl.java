package com.clinic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bbs.util.FirstWordsSqlUtils;
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
                            String sql = FirstWordsSqlUtils.getSql(name);
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
        return list(searchWrapper(val));
    }

    @Override
    public Page<Drug> search(String name,  Page<Drug> tPage) {
        return page(tPage, searchWrapper(name));
    }

    private LambdaQueryWrapper<Drug> searchWrapper(String val) {
        LambdaQueryWrapper<Drug> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .like(Drug::getManufacturer, val)
                .or().like(Drug::getDrugNo, val)
                .or().like(Drug::getApprovalNumber, val)
                .or().like(Drug::getRemark, val);
        if(StringUtils.isNotBlank(val)) {
            if(val.matches("[a-zA-Z]+")){
                queryWrapper = queryWrapper
                        .or().and(ext -> ext.likeRight(Drug::getPinYin, val).or().likeRight(Drug::getPinYinFirstLetter, val));
            } else {
                queryWrapper = queryWrapper.or().like(Drug::getName, val);
            }
        }
        return queryWrapper;
    }
}




