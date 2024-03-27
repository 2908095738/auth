package com.bbs.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bbs.entity.ResourcePage;
import com.bbs.mapper.ResourcePageMapper;
import com.bbs.service.ResourcePageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ResourcePageServiceImpl extends ServiceImpl<ResourcePageMapper, ResourcePage> implements ResourcePageService {

}
