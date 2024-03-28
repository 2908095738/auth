package com.bbs.auth.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bbs.auth.entity.ResourcePage;
import com.bbs.auth.mapper.ResourcePageMapper;
import com.bbs.auth.service.ResourcePageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ResourcePageServiceImpl extends ServiceImpl<ResourcePageMapper, ResourcePage> implements ResourcePageService {

}
