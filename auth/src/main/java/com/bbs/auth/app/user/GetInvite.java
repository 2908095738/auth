package com.bbs.auth.app.user;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.IdUtil;
import com.bbs.Result;
import com.bbs.auth.entity.Invite;
import com.bbs.auth.entity.InviteUser;
import com.bbs.auth.service.InviteService;
import com.bbs.auth.service.InviteUserService;
import com.bbs.auth.service.UserService;
import com.bbs.auth.util.LoginUser;
import com.bbs.vo.UserVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import static java.util.Objects.isNull;

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
    public Result<List<InviteUser>> getInviteCodeList() {
        return Result.success(inviteUserService.lambdaQuery().eq(InviteUser::getInitiatorUserId, LoginUser.getId()).list());
    }

    /**
     * 是否存在邀请成功
     */
    @GetMapping("/invite/check/is/invite")
    public Result<Boolean> checkIsInvite() {
        return Result.success(inviteUserService.lambdaQuery().eq(InviteUser::getInitiatorUserId, LoginUser.getId()).exists());
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