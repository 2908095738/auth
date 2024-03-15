package com.auth.service;

import com.auth.entity.ResourceType;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.clinic.Result;
import com.clinic.entity.vo.BaseParam;

/**
* @author 路晨霖
* @description 针对表【resource_type(资源类型)】的数据库操作Service
* @createDate 2023-12-28 18:09:18
*/
public interface ResourceTypeService extends IService<ResourceType> {

    Result<Page<ResourceType>> searchJoin(BaseParam param);

    Result<Page<ResourceType>> search(BaseParam param);
}
