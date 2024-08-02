package com.clinic.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clinic.entity.AuxiliaryType;
import com.clinic.mapper.AuxiliaryTypeMapper;
import com.clinic.service.AuxiliaryTypeService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 */
@Service
public class AuxiliaryTypeServiceImpl extends ServiceImpl<AuxiliaryTypeMapper, AuxiliaryType>
    implements AuxiliaryTypeService{

    @Override
    public List<AuxiliaryType> search() {
        return list();
    }
}




