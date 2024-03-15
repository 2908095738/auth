package com.auth.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.auth.entity.Group;
import com.auth.mapper.GroupMapper;
import org.springframework.stereotype.Service;
import com.auth.service.GroupService;

/**
 *
 */
@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, Group>
    implements GroupService {

}




