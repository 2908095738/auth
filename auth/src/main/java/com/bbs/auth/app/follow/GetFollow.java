package com.bbs.auth.app.follow;

import cn.hutool.core.collection.CollUtil;
import com.bbs.Result;
import com.bbs.auth.entity.Fan;
import com.bbs.auth.service.FanService;
import com.bbs.auth.service.UserService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.Resource;
import java.util.List;

public class GetFollow {

    @Resource
    private FanService fanService;
    @Resource
    private UserService userService;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DTO {

        private Long userId;

        private String userName;

        private String url;
    }

    /**
     * 查询关注
     * @return Boolean
     */
    @GetMapping("/follow")
    public Result<List<DTO>> getFollow(){
        List<Fan> list = fanService.getFollow(userService.loginUser().getId());
        if(CollUtil.isNotEmpty(list)){
//            List<GetFollowOrFanDto> result = list.stream().map(o -> new GetFollowOrFanDto(o.getFollowUserId(), , )).collect(Collectors.toList());
//            return Result.success(result);
        }
        return Result.failedNull();
    }
}
