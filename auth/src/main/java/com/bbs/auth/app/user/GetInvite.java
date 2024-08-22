package com.bbs.auth.app.user;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.IdUtil;
import com.bbs.Result;
import com.bbs.auth.entity.Invite;
import com.bbs.auth.service.InviteService;
import com.bbs.auth.util.LoginUser;
import com.bbs.auth.util.ORMUtil;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Objects;

/**
 * 获取邀请码
 */
@RestController
@RequestMapping
public class GetInvite {

    @Resource
    private InviteService orm;

    @Resource
    private TransactionDefinition transactionDefinition;
    @Resource
    private DataSourceTransactionManager transactionManager;

    private static class Constant {
        public static final Long VALID_TIME_LONG = 7L * 24 * 60 * 60 * 1000;//邀请码有效时间
    }

    /**
     * 获取邀请码
     */
    @GetMapping("/user/invite")
    public Result<String> getInviteCode() {
        Invite invite = orm.selectJoinOne(Invite.class, new MPJLambdaWrapper<Invite>()
                .eq(Invite::getUserId, LoginUser.getId()));

        if (Objects.nonNull(invite)) {//已生成邀请码
            if (invite.getValidEndTime().getTime() < new Date().getTime()) {//邀请码已过期
                ORMUtil.fastTran(() -> orm.remove(new MPJLambdaWrapper<Invite>()
                        .eq(Invite::getUserId, LoginUser.getId())), transactionManager, transactionDefinition);

                invite = getInv();
                final Invite toDBInvite = invite;
                ORMUtil.fastTran(() -> orm.save(toDBInvite), transactionManager, transactionDefinition);
            }

            return Result.success(invite.getInviteCode());
        } else {//未生成邀请码
            Invite toDBInvite = getInv();
            ORMUtil.fastTran(() -> orm.save(toDBInvite), transactionManager, transactionDefinition);
            return Result.success(toDBInvite.getInviteCode());
        }
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