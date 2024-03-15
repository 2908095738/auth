package com.auth.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.auth.entity.ResourcePage;
import com.auth.mapper.ResourcePageMapper;
import com.auth.service.ResourcePageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ResourcePageServiceImpl extends ServiceImpl<ResourcePageMapper, ResourcePage> implements ResourcePageService {

}
