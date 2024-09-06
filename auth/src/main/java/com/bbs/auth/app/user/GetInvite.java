package com.bbs.auth.app.user;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.IdUtil;
import com.bbs.Result;
import com.bbs.auth.entity.Invite;
import com.bbs.auth.service.InviteService;
import com.bbs.auth.util.LoginUser;
import com.bbs.auth.util.ORMUtil;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
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

        public static final String TOTAL = "total";//条数别名

        public static final Integer MAT_TOTAL = 5;//最大未用条数

        public static final String FAIL_CODE = "生成邀请码失败";
    }

    /**
     * 获取邀请码
     */
    @GetMapping("/invite")
    public Result<String> getInviteCode() {
        List<Invite> tmpList = getLast();
        Integer total = tmpList.size();
        Invite lastInv = null;
        if (!tmpList.isEmpty())
            lastInv = tmpList.get(total - NumberUtils.INTEGER_ONE);

        if (Objects.nonNull(lastInv) && tmpList.size() < Constant.MAT_TOTAL) {//未使用的前四条邀请码
            Invite toDBInvite = getInv();
            ORMUtil.fastTran(() -> orm.save(toDBInvite), transactionManager, transactionDefinition);
            return Result.success(toDBInvite.getInviteCode());
        } else if (Objects.nonNull(lastInv) && tmpList.size() == Constant.MAT_TOTAL) {//未使用的第五条邀请码
            if (lastInv.getValidEndTime().getTime() < new Date().getTime()) {//邀请码已过期
                Long lastId = lastInv.getId();
                ORMUtil.fastTran(() -> orm.remove(
                        new MPJLambdaWrapper<Invite>()
                                .eq(Invite::getId, lastId)
                ), transactionManager, transactionDefinition);
            } else
                return Result.success(lastInv.getInviteCode());

            Invite toDBInvite = getInv();
            ORMUtil.fastTran(() -> orm.save(toDBInvite), transactionManager, transactionDefinition);
            return Result.success(toDBInvite.getInviteCode());
        } else if (Objects.isNull(lastInv)) {//未使用的第一条邀请码
            Invite toDBInvite = getInv();
            ORMUtil.fastTran(() -> orm.save(toDBInvite), transactionManager, transactionDefinition);
            return Result.success(toDBInvite.getInviteCode());
        }
        return Result.failed(Constant.FAIL_CODE);
    }

    /**
     * 获取最新邀请码
     */
    private List<Invite> getLast() {
        return orm.selectJoinList(Invite.class,
                new MPJLambdaWrapper<Invite>()
                        .eq(Invite::getUserId, LoginUser.getId())
                        .isNull(Invite::getInviteUserId)
                        .orderByAsc(Invite::getCreateTime));
    }

    /**
     * 获取邀请码
     */
    @GetMapping("/invite/list")
    public Result<List<Invite>> getInviteCodeList() {
        return Result.success(orm.lambdaQuery().eq(Invite::getUserId, LoginUser.getId()).list());
    }

    /**
     * 是否存在邀请成功
     */
    @GetMapping("/invite/check/is/invite")
    public Result<Boolean> checkIsInvite() {
        return Result.success(orm.lambdaQuery().eq(Invite::getUserId, LoginUser.getId()).isNotNull(Invite::getInviteUserId).exists());
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