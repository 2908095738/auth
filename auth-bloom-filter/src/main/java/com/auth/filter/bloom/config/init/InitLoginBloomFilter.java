package com.auth.filter.bloom.config.init;

import com.auth.user.User;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class InitLoginBloomFilter implements InitializingBean {

    @Resource
    private User.Search searchUser;

    @Resource
    private RedissonClient redisson;

    @Override
    public void afterPropertiesSet() {
//        List<Long> userIDList = searchUser.searchAllID();
//        RBloomFilter<Long> bloomFilter = redisson.getBloomFilter("user-id-bloom-filter");
//        bloomFilter.tryInit(1000000, 0.01);
//        bloomFilter.add(userIDList);
    }
}
