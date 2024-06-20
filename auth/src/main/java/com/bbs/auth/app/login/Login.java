package com.bbs.auth.app.login;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hibernate.validator.constraints.Length;
import org.redisson.api.RDeque;
import org.redisson.api.RedissonClient;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Param {

        /**
         * 手机号
         */
        @NotBlank
        @Length(max = 11, min = 11, message = "手机号格式错误")
        private String phone;

        /**
         * 验证码
         */
        private String code;

        private String password;

        /**
         * 登录类型
         */
        private Integer loginType;

        /**
         * 是否查询用户公司信息
         */
        private Boolean searchCompany;

        /**
         * 是否检查公司结构（增加【是否设置公司结构】的查询结果）
         * 注：需要 searchCompany = true
         */
        private Boolean checkCompanyStructure;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VO {
        /**
         * 用户ID
         */
        private Long uid;
        /**
         * 用户名称
         */
        private String name;

        private String token;

        /**
         * 用户公司
         */
        List<UserCompany> userCompanyList;

        /**
         * 是否设置了公司结构
         */
        Boolean isSettingCompanyStructure;

        public VO(Long uid, String name, String token, List<UserCompany> userCompanyList) {
            this.uid = uid;
            this.name = name;
            this.token = token;
            this.userCompanyList = userCompanyList;
        }
    }

    @PostMapping("/login")
    public Result<VO> login(@Valid @RequestBody Param param) throws InterruptedException, IllegalArgumentException {
        String loginTime = DateUtil.now();
        String phone = param.getPhone();
        return redissonUtil.lockExec(
            () -> {
                try {
                    log.debug("[Login::login] param={}", JSONUtil.toJsonPrettyStr(param));
                    User user;
                    Integer code;
                    checkArgument(LoginType.checkFormat(param.loginType), FAILED_LOGIN_TYPE_NOT_AVAILABLE);
                    if(LoginType.PHONE.getCode().equals(param.loginType)) {
                        checkPhoneFormat(phone);
                        checkPhoneCodeFormat(param.code);
                        code = phoneCodeCache.getCode(phone);
                        checkArgument(nonNull(code) && code.equals(Integer.valueOf(param.code)), FAILED_AUTH_PHONE_CODE_NOT_AVAILABLE);
                        phoneCodeCache.delCode(phone);
                        user = userCache.searchByPhoneNoLockNoLoad(phone);

                        checkArgument(nonNull(user), FAILED_LOGIN_USER_NOT_EXISTS);
                        checkUserState(user);

                    } else if (LoginType.WX.getCode().equals(param.loginType)) {
                        throw new IllegalArgumentException("微信登录未开通");

                    } else if(LoginType.PASSWORD.getCode().equals(param.loginType)) {
                        checkPhoneAndPWDFormat(param);
                        user = searchUser(phone);
                        checkArgument(nonNull(user), FAILED_LOGIN_USER_NOT_EXISTS);
                        checkUserState(user);
                        checkUserPWD(param, user);
                    } else if(LoginType.PASSWORD_CREATE.getCode().equals(param.loginType)) {
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
                    searchIsSetCompanyStructure(param.checkCompanyStructure, userCompanyList);

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
                redisson.getSpinLock(USER_LOGIN_PHONE.LOCK.key(param.phone)),
                50000,
                50000,
                MILLISECONDS
        );
    }

    private void checkPhoneAndPWDFormat(Param param) {
        checkPhoneFormat(param.phone);
        checkArgument(StringUtils.isNoneBlank(param.password));
    }

    private void checkUserPWD(Param param, User user) throws IllegalArgumentException {
        String encryptPassword = service.encryptPassword(param.password, user.getSalt());
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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginLog {

        /**
         * 登录结果（0正常/1失败）
         */
        private Integer result;

        /**
         * 手机号
         */
        private String phone;

        /**
         * 输入验证码
         */
        private String code;

        /**
         * 登录类型
         */
        private Integer loginType;

        /**
         * 服务端保存的手机验证码
         */
        private Integer serverSavePhoneCode;

        /**
         * 登录成功生成的 Token
         */
        private String newToken;

        /**
         * 失败原因
         */
        private String errorMsg;

        /**
         * 登录时间
         */
        private String loginTime;

        public LoginLog(Integer result, Param param, String loginTime) {
            this.result = result;
            this.phone = param.phone;
            this.code = param.code;
            this.loginType = param.loginType;
            this.loginTime = loginTime;
        }
    }

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
        LoginLog log = new LoginLog(NumberUtils.INTEGER_ZERO, param, loginTime);
        log.setNewToken(newToken);
        deque().addFirst(JSONUtil.toJsonPrettyStr(log));
    }

    public void recordLoginFailLog(Param param, String errorMsg, String loginTime) {
        LoginLog log = new LoginLog(NumberUtils.INTEGER_ZERO, param, loginTime);
        log.setErrorMsg(errorMsg);
        deque().addFirst(JSONUtil.toJsonPrettyStr(log));
    }
}
