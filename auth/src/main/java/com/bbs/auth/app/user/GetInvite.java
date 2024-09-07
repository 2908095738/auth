package com.bbs.auth.app.user;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.auth.entity.Invite;
import com.bbs.auth.entity.InviteUser;
import com.bbs.auth.entity.User;
import com.bbs.auth.enums.InviteClaimStatus;
import com.bbs.auth.service.InviteService;
import com.bbs.auth.service.InviteUserService;
import com.bbs.auth.service.UserService;
import com.bbs.auth.util.LoginUser;
import com.bbs.vo.UserVO;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;

import static com.bbs.auth.enums.InviteClaimStatus.*;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ONE;
import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ZERO;

/**
 * 获取邀请码
 */
@RestController
@RequestMapping
public class GetInvite {

    @Resource
    private InviteService orm;

    @Resource
    private InviteUserService inviteUserService;
    @Resource
    private UserService userService;

    private static class Constant {
        public static final Long VALID_TIME_LONG = 7L * 24 * 60 * 60 * 1000;//邀请码有效时间

        public static final String TOTAL = "total";//条数别名

        public static final Integer MAT_TOTAL = 5;//最大未用条数

        public static final String FAIL_CODE = "生成邀请码失败";
    }

    /**
     * 获取邀请码
     */
    @Transactional
    @GetMapping("/invite")
    public Result<String> getInviteCode() {
        Invite invite = orm.search();
        if(isNull(invite)) {
            UserVO loginUser = userService.loginUser();
            invite = new Invite(loginUser.getId(), IdUtil.randomUUID());
            orm.save(invite);
        }
        return Result.success(invite.getInviteCode());
    }

    /**
     * 获取已邀请用户
     */
    @GetMapping("/invite/list")
    public Result<Page<InviteUser>> getInviteCodeList(@RequestParam(required = false, defaultValue = "1") Integer current, @RequestParam(required = false, defaultValue = "10") Integer size) {
        Page<InviteUser> inviteUserPage = inviteUserService.lambdaQuery().eq(InviteUser::getInitiatorUserId, LoginUser.getId()).page(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(current, size));
        List<InviteUser> inviteUserList = inviteUserPage.getRecords();
        if(inviteUserList.size() > INTEGER_ZERO) {
            Set<Long> userIds = new HashSet<>();
            inviteUserList.forEach(inviteUser -> {
                userIds.add(inviteUser.getInvitedUserId());
                userIds.add(inviteUser.getInitiatorUserId());
            });
            Map<Long, User> userMap = userService.searchMap(userIds);
            if(nonNull(userMap) && userMap.size() > INTEGER_ZERO) {
                inviteUserList.forEach(inviteUser -> {
                    inviteUser.setInvitedUser(userMap.get(inviteUser.getInvitedUserId()));
                    inviteUser.setInitiatorUser(userMap.get(inviteUser.getInitiatorUserId()));
                });
            }
        }
        return Result.success(inviteUserPage);
    }

    /**
     * 检查初次邀请任务的完成、奖励领取状态
     */
    @GetMapping("/invite/check/is/invite")
    public Result<Integer> checkIsInvite() {
        InviteClaimStatus result = UNFINISHED;
        List<InviteUser> inviteUserList = searchInviteList();
        if(nonNull(inviteUserList)) {
            // 判断是否完成任务
            if(isCompleteTask(inviteUserList)) {
                result = UNCLAIMED_AWARD;
            }
            // 判断是否已领取奖励
            for (InviteUser inviteUser : inviteUserList) {
                if(isAwardReceived(inviteUser)) {
                    result = AWARD_RECEIVED;
                    break;
                }
            }
        }
        return Result.success(result.getCode());
    }

    private Boolean isCompleteTask(List<InviteUser> inviteUserList) {
        return inviteUserList.size() > INTEGER_ZERO;
    }

    private Boolean isAwardReceived(InviteUser inviteUser) {
        Integer rewardState = inviteUser.getReward();
        return nonNull(rewardState) && AWARD_RECEIVED.getCode().equals(rewardState);
    }

    private List<InviteUser> searchInviteList() {
        User loginUser = userService.loginEntityUser();
        Long loginUserID = loginUser.getId();
        return inviteUserService.lambdaQuery()
                .eq(InviteUser::getInitiatorUserId, loginUserID)
                .list();
    }

    /**
     * 获取邀请实例
     */
    private Invite getInv() {
        Invite toDBInvite = new Invite();
        toDBInvite.setUserId(LoginUser.getId());
        toDBInvite.setInviteCode(IdUtil.fastSimpleUUID());
        toDBInvite.setValidEndTime(new Date(DateTime.now().getTime() + Constant.VALID_TIME_LONG));
        return toDBInvite;
    }
}