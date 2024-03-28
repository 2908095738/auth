package com.bbs.auth.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bbs.auth.entity.Group;
import com.bbs.auth.mapper.GroupMapper;
import org.springframework.stereotype.Service;
import com.bbs.auth.service.GroupService;

/**
 *
 */
@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, Group>
    implements GroupService {

}




