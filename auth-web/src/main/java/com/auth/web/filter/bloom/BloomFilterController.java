package com.auth.web.filter.bloom;

import com.auth.Result;
import com.auth.user.User;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RestController
public class BloomFilterController {

    @Resource
    private User.Search searchUser;

    @Resource
    private RedissonClient redisson;

    @PostMapping("/bloom/filter/init/user/id")
    public Result<Boolean> init(
            @RequestParam(required = false, defaultValue = "1000000") long expectedInsertions,
            @RequestParam(required = false, defaultValue = "0.01") double falseProbability
    ) {
        log.info("布隆过滤器 - 用户ID - 开始初始化布隆过滤器(预期数量: {}; 预期准确率={};)...", expectedInsertions, falseProbability);
        List<Long> userIDList = searchUser.searchAllID();
        RBloomFilter<Long> bloomFilter = redisson.getBloomFilter("user-id-bloom-filter");
        bloomFilter.tryInit(expectedInsertions, falseProbability);
        bloomFilter.add(userIDList);
        log.info("布隆过滤器 - 用户ID - 初始化完成！数量={}", bloomFilter.count());
        return Result.success();
    }
}
