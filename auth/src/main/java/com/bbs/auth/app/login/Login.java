package com.bbs.auth.app.login;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.bbs.Result;
import com.bbs.auth.app.login.param.Param;
import com.bbs.auth.app.login.vo.VO;
import com.bbs.auth.cache.code.PhoneCodeCache;
import com.bbs.auth.cache.user.UserCache;
import com.bbs.auth.dao.UserDao;
import com.bbs.auth.entity.*;
import com.bbs.auth.service.*;
import com.bbs.auth.util.RedisUtil;
import com.bbs.auth.util.WxUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.bbs.Result.failed;
import static com.bbs.Result.success;
import static com.bbs.auth.enums.RedisKeys.USER_LOGIN_PHONE;
import static com.bbs.enums.CodeEnum.*;
import static java.util.Objects.nonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ZERO;

@Slf4j
@RestController
@RequestMapping
public class Login {

    @Resource
    private UserCache userCache;

    @Resource
    private TokenService tokenService;

    @Resource
    private RedisUtil.Redisson redissonUtil;

    @Resource
    private RedissonClient redisson;
    @Resource
    private UserDao db;
    @Resource
    private CompanyService companyService;

    @Resource
    private LoginLogService logService;

    @Resource
    private WxUtil wxUtil;

    @Value("${wx.oa.token}")
    private String token;

    @Resource
    private InviteService inviteService;

    @Resource
    private InviteUserService inviteUserService;

    @Resource
    private LoginStrategy loginStrategy;

    @PostMapping("/login")
    public Result<VO> login(@Valid @RequestBody Param param) throws InterruptedException, IllegalArgumentException {
        String loginTime = DateUtil.now();
        String phone = param.getPhone();
        String loginType = param.getLoginType();
        return redissonUtil.lockExec(
            () -> {
                try {

                    User user = loginStrategy.getInstance(loginType).tryLogin(param);
                    Date expirationTime = user.getExpirationTime();
                    if(nonNull(expirationTime)) {
                        long between = DateUtil.between(expirationTime, new Date(), DateUnit.DAY);
                        if(between >= INTEGER_ZERO) {
                            // 正常
                            if(between <= 3) {
                                // 即将过期
                                // TODO 向用户发送通知
                            }
                        } else {
                            // 过期
                            throw new IllegalArgumentException(FAILED_ACCOUNT_EXPIRED.getMsg());
                        }
                    }

                    List<UserCompany> userCompanyList = searchUserCompany(user.getId());
                    searchIsSetCompanyStructure(param.getCheckCompanyStructure(), userCompanyList);

                    String token = tokenService.createToken(user);
                    tokenService.setLoginFlag(user.getId(), param.getExpireNumber(), TimeUnit.DAYS);
                    userCache.expireUserAndPhoneMap(user);

                    // 邀请相关
                    String inviteCode = param.getInviteCode();
                    if(StringUtils.isNoneBlank(inviteCode)) {
                        Invite invite = inviteService.lambdaQuery().eq(Invite::getInviteCode, inviteCode).one();
                        if(nonNull(invite)) {
                            inviteUserService.save(new InviteUser(invite.getUserId(), user.getId(), inviteCode));
                        }
                    }
                    recordLoginSuccessLog(param, token, loginTime);
                    return success(new VO(user.getId(), user.getName(), token, userCompanyList));
                } catch (IllegalArgumentException e) {
                    recordLoginFailLog(param, e.getMessage(), loginTime);
                    throw e;
                }
            },
            () -> failed(500, new VO(), "无法获取登录锁，详情请联系客服"),
                redisson.getSpinLock(USER_LOGIN_PHONE.LOCK.key(phone)),
                50000,
                50000,
                MILLISECONDS
        );
    }

    private List<UserCompany> searchUserCompany(Long uid) {
        return companyService.searchCompany(uid);
    }

    private void searchIsSetCompanyStructure(boolean checkCompanyStructure, List<UserCompany> userCompanyList) {
        if(checkCompanyStructure) {
            for (UserCompany userCompany : userCompanyList) {
                Company company = userCompany.getCompany();
                company.setIsSetCompanyStructure(companyService.searchIsSetCompanyStructure(userCompany.getCompanyId()));
            }
        }
    }

    public void recordLoginSuccessLog(Param param, String newToken, String loginTime) {
        LoginLog log = new LoginLog(INTEGER_ZERO, param, loginTime);
        log.setNewToken(newToken);
        logService.save(log);
    }

    public void recordLoginFailLog(Param param, String errorMsg, String loginTime) {
        LoginLog log = new LoginLog(INTEGER_ZERO, param, loginTime);
        log.setErrorMsg(errorMsg);
        logService.save(log);
    }
}
