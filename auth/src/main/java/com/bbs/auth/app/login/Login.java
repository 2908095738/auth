package com.bbs.auth.app.login;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.bbs.auth.app.login.param.Param;
import com.bbs.auth.app.login.vo.VO;
import com.bbs.auth.cache.user.UserCache;
import com.bbs.auth.dao.UserDao;
import com.bbs.auth.entity.Company;
import com.bbs.auth.entity.UserCompany;
import com.bbs.auth.service.CompanyService;
import com.bbs.auth.service.UserService;
import com.bbs.auth.util.RedisUtil;
import com.bbs.Result;
import com.bbs.auth.cache.code.PhoneCodeCache;
import com.bbs.auth.entity.User;
import com.bbs.enums.LoginType;
import com.bbs.enums.UserStateEnum;
import com.bbs.auth.service.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.redisson.api.RDeque;
import org.redisson.api.RedissonClient;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import java.util.List;

import static com.bbs.Result.failed;
import static com.bbs.Result.success;
import static com.bbs.auth.util.PhoneUtil.checkPhoneCodeFormat;
import static com.bbs.auth.util.PhoneUtil.checkPhoneFormat;
import static com.bbs.auth.enums.RedisKeys.USER_LOGIN_PHONE;
import static com.bbs.enums.CodeEnum.*;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Slf4j
@RestController
@RequestMapping
public class Login {

    @Resource
    private UserCache userCache;
    @Resource
    private UserService service;
    @Resource
    private PhoneCodeCache phoneCodeCache;

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

    @PostMapping("/login")
    public Result<VO> login(@Valid @RequestBody Param param) throws InterruptedException, IllegalArgumentException {
        String loginTime = DateUtil.now();
        String phone = param.getPhone();
        Integer loginType = param.getLoginType();
        String paramCode = param.getCode();
        return redissonUtil.lockExec(
            () -> {
                try {
                    log.debug("[Login::login] param={}", JSONUtil.toJsonPrettyStr(param));
                    User user;
                    Integer code;
                    checkArgument(LoginType.checkFormat(loginType), FAILED_LOGIN_TYPE_NOT_AVAILABLE);
                    if(LoginType.PHONE.getCode().equals(loginType)) {
                        checkPhoneFormat(phone);
                        checkPhoneCodeFormat(paramCode);
                        code = phoneCodeCache.getCode(phone);
                        checkArgument(nonNull(code) && code.equals(Integer.valueOf(paramCode)), FAILED_AUTH_PHONE_CODE_NOT_AVAILABLE);
                        phoneCodeCache.delCode(phone);
                        user = userCache.searchByPhoneNoLockNoLoad(phone);

                        checkArgument(nonNull(user), FAILED_LOGIN_USER_NOT_EXISTS);
                        checkUserState(user);

                    } else if (LoginType.WX.getCode().equals(loginType)) {
                        throw new IllegalArgumentException("微信登录未开通");

                    } else if(LoginType.PASSWORD.getCode().equals(loginType)) {
                        checkPhoneAndPWDFormat(param);
                        user = searchUser(phone);
                        checkArgument(nonNull(user), FAILED_LOGIN_USER_NOT_EXISTS);
                        checkUserState(user);
                        checkUserPWD(param, user);
                    } else if(LoginType.PASSWORD_CREATE.getCode().equals(loginType)) {
                        checkPhoneAndPWDFormat(param);
                        user = searchUser(phone);
                        if(nonNull(user)) {
                            // 用户已注册
                            checkUserState(user);
                            checkUserPWD(param, user);
                        } else {
                            // 用户未注册
                            return failed(FAILED_LOGIN_USER_NEED_REGISTER);
                        }
                    } else {
                        return failed(FAILED_LOGIN_TYPE_NOT_AVAILABLE);
                    }
                    List<UserCompany> userCompanyList = searchUserCompany(user.getId());
                    searchIsSetCompanyStructure(param.getCheckCompanyStructure(), userCompanyList);



                    String token = tokenService.createToken(user);
                    tokenService.setLoginFlag(user.getId());
                    userCache.expireUserAndPhoneMap(user);
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

    private void checkPhoneAndPWDFormat(Param param) {
        checkPhoneFormat(param.getPhone());
        checkArgument(StringUtils.isNoneBlank(param.getPassword()));
    }

    private void checkUserPWD(Param param, User user) throws IllegalArgumentException {
        String encryptPassword = service.encryptPassword(param.getPassword(), user.getSalt());
        checkArgument(user.getPassword().equals(encryptPassword), FAILED_LOGIN_PWD_ERROR);
    }

    private void checkUserState(User user) throws IllegalArgumentException {
        checkArgument(UserStateEnum.STATUS_NORMAL.getCode().equals(user.getState()), FAILED_LOGIN_USER_STATUS_ERROR);
    }

    private User searchUser(String phone) {
        User user = userCache.searchByPhoneNoLockNoLoad(phone);
        if(isNull(user)) user = db.selectByPhone(phone);
        return user;
    }

    private static final String LOG_DEQUE_KEY = "LOG:LOGIN";

    private List<UserCompany> searchUserCompany(Long uid) {
        return companyService.searchCompany(uid);
    }

    private RDeque<String> deque() {
        return redisson.getDeque(LOG_DEQUE_KEY);
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
        Log log = new Log(NumberUtils.INTEGER_ZERO, param, loginTime);
        log.setNewToken(newToken);
        deque().addFirst(JSONUtil.toJsonPrettyStr(log));
    }

    public void recordLoginFailLog(Param param, String errorMsg, String loginTime) {
        Log log = new Log(NumberUtils.INTEGER_ZERO, param, loginTime);
        log.setErrorMsg(errorMsg);
        deque().addFirst(JSONUtil.toJsonPrettyStr(log));
    }
}
