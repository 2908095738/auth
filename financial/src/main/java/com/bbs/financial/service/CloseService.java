package com.bbs.financial.service;

import com.bbs.financial.entity.Close;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bbs.financial.entity.CloseType;

import java.util.List;

/**
* @author 路晨霖
* @description 针对表【asset_close(结账)】的数据库操作Service
* @createDate 2024-06-25 17:10:57
*/
public interface CloseService extends IService<Close> {

    List<CloseType> searchCloseType(Long accountingSetId);
}
