package com.bbs.content.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bbs.content.entity.OperationLog;
import com.bbs.content.mapper.OperationLogMapper;
import com.bbs.content.service.OperationLogService;
import org.springframework.stereotype.Service;

/**
* @author 路晨霖
* @description 针对表【log_operation(操作日志)】的数据库操作Service实现
* @createDate 2023-12-30 18:34:19
*/
@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog>
    implements OperationLogService {

}




