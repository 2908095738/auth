package com.bbs.financial.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bbs.financial.entity.Close;
import com.bbs.financial.service.AssetCloseService;
import com.bbs.financial.mapper.AssetCloseMapper;
import org.springframework.stereotype.Service;

/**
* @author 路晨霖
* @description 针对表【asset_close(结账)】的数据库操作Service实现
* @createDate 2024-06-25 17:10:57
*/
@Service
public class AssetCloseServiceImpl extends ServiceImpl<AssetCloseMapper, Close>
    implements AssetCloseService{

}




