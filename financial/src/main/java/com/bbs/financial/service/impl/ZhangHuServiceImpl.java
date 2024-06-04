package com.bbs.financial.service.impl;

import com.bbs.financial.entity.ZhangHu;
import com.bbs.financial.service.ZhangHuService;
import com.bbs.financial.mapper.ZhangHuMapper;
import com.github.yulichang.base.MPJBaseServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @author Mafty
 * @description 针对表【zhang_hu(账户)】的数据库操作Service实现
 * @createDate 2024-05-21 12:20:53
 */
@Service
public class ZhangHuServiceImpl extends MPJBaseServiceImpl<ZhangHuMapper, ZhangHu>
        implements ZhangHuService {
}