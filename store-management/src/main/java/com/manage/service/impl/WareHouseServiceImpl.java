package com.manage.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bbs.enums.CommonStatusEnum;
import com.bbs.util.BeanUtils;
import com.manage.dto.warehouse.ErpWarehousePageReqVO;
import com.manage.dto.warehouse.ErpWarehouseSaveReqVO;
import com.manage.entity.WareHouse;
import com.manage.mapper.WareHouseMapper;
import com.manage.service.WareHouseService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.bbs.util.CollectionUtils.convertMap;


/**
 * ERP 仓库 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class WareHouseServiceImpl  extends ServiceImpl<WareHouseMapper, WareHouse> implements WareHouseService {


    @Override
    public Long createWarehouse(ErpWarehouseSaveReqVO createReqVO) {
        // 插入
        WareHouse warehouse = BeanUtils.toBean(createReqVO, WareHouse.class);
        baseMapper.insert(warehouse);
        // 返回
        return warehouse.getId();
    }

    @Override
    public void updateWarehouse(ErpWarehouseSaveReqVO updateReqVO) {
        // 校验存在
        validateWarehouseExists(updateReqVO.getId());
        // 更新
        WareHouse updateObj = BeanUtils.toBean(updateReqVO, WareHouse.class);
        baseMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWarehouseDefaultStatus(Long id, Integer defaultStatus) {
        // 1. 校验存在
        validateWarehouseExists(id);

        // 2.1 如果开启，则需要关闭所有其它的默认
        if (defaultStatus==1) {
            WareHouse warehouse = lambdaQuery().eq(WareHouse::getDefaultStatus,defaultStatus).eq(WareHouse::getId,id).one();
            if (warehouse != null) {
                baseMapper.updateById(new WareHouse().setId(warehouse.getId()).setDefaultStatus(1));
            }
        }
        // 2.2 更新对应的默认状态
        baseMapper.updateById(new WareHouse().setId(id).setDefaultStatus(defaultStatus));
    }

    @Override
    public void deleteWarehouse(Long id) {
        // 校验存在
        validateWarehouseExists(id);
        // 删除
        baseMapper.deleteById(id);
    }

    private void validateWarehouseExists(Long id) {
        if (baseMapper.selectById(id) == null) {
            throw new RuntimeException();
        }
    }

    @Override
    public WareHouse getWarehouse(Long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public List<WareHouse> validWarehouseList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<WareHouse> list = baseMapper.selectBatchIds(ids);
        Map<Long, WareHouse> warehouseMap = convertMap(list, WareHouse::getId);
        for (Long id : ids) {
            WareHouse warehouse = warehouseMap.get(id);
            if (warehouseMap.get(id) == null) {
                throw new RuntimeException();
            }
            if (CommonStatusEnum.isDisable(warehouse.getStatus())) {
                throw new RuntimeException();
            }
        }
        return list;
    }

    @Override
    public List<WareHouse> getWarehouseListByStatus(Integer status) {
        return lambdaQuery()
                .eq(WareHouse::getStatus,status)
                .eq(WareHouse::getCreateId,1l)
                .list();
    }

    @Override
    public List<WareHouse> getWarehouseList(Collection<Long> ids) {
        return baseMapper.selectBatchIds(ids);
    }

    @Override
    public Page<WareHouse> getWarehousePage(ErpWarehousePageReqVO pageReqVO) {
        LambdaQueryChainWrapper<WareHouse> eq = lambdaQuery()
                .eq(StringUtils.isNotEmpty(pageReqVO.getName()), WareHouse::getName, pageReqVO.getName())
                .eq(Objects.nonNull(pageReqVO.getStatus()), WareHouse::getStatus, pageReqVO.getStatus())
                .eq(WareHouse::getCreateId, 1l);
        if(Objects.nonNull(pageReqVO.getStatus())){
            return eq.page(pageReqVO.toPage());
        }{
            return new Page<WareHouse>().setRecords(eq.list());
        }
    }

}