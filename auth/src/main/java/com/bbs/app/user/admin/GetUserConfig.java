package com.bbs.app.user.admin;

import com.bbs.cache.UserCache;
import com.bbs.entity.User;
import com.bbs.entity.UserGroup;
import com.bbs.service.UserGroupService;
import com.clinic.Result;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Date;

import static com.clinic.Result.failed;
import static com.clinic.Result.success;
import static java.util.Objects.nonNull;

@Slf4j
@RestController
@RequestMapping
public class GetUserConfig {

    @Resource
    private UserCache cache;

    @Resource
    private UserGroupService userGroupService;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class UserConfigVO {

        private Date expirationTime;

        private Long groupId;
    }

    @GetMapping("/user/admin")
    public Result<UserConfigVO> getUserConfig(@Valid @NotNull Long id) {
        try {
            User user = cache.search(id);
            UserGroup one = userGroupService.lambdaQuery().eq(UserGroup::getUserId, id).one();
            Long groupId = nonNull(one) ? one.getGroupId() : null;
            return success(new UserConfigVO(user.getExpirationTime(), groupId));
        } catch (Exception e) {
            e.printStackTrace();
            return failed(e.getMessage());
        }
    }
}
