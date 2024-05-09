package com.bbs.content.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bbs.content.entity.Audit;
import com.bbs.content.service.AuditService;
import com.bbs.content.mapper.AuditMapper;
import org.springframework.stereotype.Service;

/**
* @author 路晨霖
* @description 针对表【audit(内容审核)】的数据库操作Service实现
* @createDate 2024-05-05 15:24:14
*/
@Service
public class AuditServiceImpl extends ServiceImpl<AuditMapper, Audit>
    implements AuditService{

}




