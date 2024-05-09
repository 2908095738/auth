package com.bbs.stream.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bbs.Result;
import com.bbs.stream.dto.StreamDto;
import com.bbs.stream.entity.Stream;

public interface StreamService extends IService<Stream> {

    /**
     * 创建审批
     *
     * @param companyId 公司id
     * @param stream    审批实例
     * @return
     */
    Result create(Long companyId, Stream stream);

    /**
     * 获取审批列表
     *
     * @param current 页码
     * @param size    条数
     * @param status  审批状态: 0.已审批;1.待审批
     * @param type    审批类型: 1.请假;
     * @param topUid  审批用户id
     * @return
     */
    Page<StreamDto> list(Integer current, Integer size, Integer status, Integer type, Long topUid);
}