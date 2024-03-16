package com.bbs.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bbs.entity.OperationLog;
import com.bbs.mapper.OperationLogMapper;
import com.bbs.service.OperationLogService;
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




