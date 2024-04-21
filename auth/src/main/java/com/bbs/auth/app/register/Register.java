package com.bbs.auth.app.register;

import com.bbs.auth.cache.code.PhoneCodeCache;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bbs.Result;
import com.bbs.auth.converter.UserConverter;
import com.bbs.auth.dao.UserDao;
import com.bbs.auth.entity.User;
import com.bbs.auth.mapper.UserMapper;
import com.bbs.auth.service.UserService;
import com.bbs.enums.UserStateEnum;
import lombok.Data;
import net.sf.jsqlparser.util.validation.metadata.DatabaseException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import java.util.Random;

import static com.bbs.Result.failed;
import static com.bbs.Result.success;
import static com.bbs.enums.CodeEnum.FAILED_USER_CODE_NOT_AVAILABLE;
import static com.bbs.enums.CodeEnum.FAILED_USER_INFO_DUPLICATION;
import static com.google.common.base.Preconditions.checkArgument;

@RestController
@RequestMapping
public class Register extends ServiceImpl<UserMapper, User> {

    @Resource
    private DataSourceTransactionManager transactionManager;
    @Resource
    private TransactionDefinition transactionDefinition;

    @Resource
    private UserConverter converter;
    @Resource
    private UserDao dao;
    @Resource
    private PhoneCodeCache phoneCodeCache;
    @Resource
    private UserService service;
    @Data
    public static class Param {

        private String userName;

        private String clinicName;

        private String password;

        @NotNull(message = "手机号不能为空")
        private Long phone;

        @NotNull(message = "验证码不能为空")
        private Integer code;

        private String email;

        private Long group;

        /**
         * 平台角色
         */
        private Integer paasRole;
    }


    /**
     * 用户注册
     * @param param 注册用户入参
     * @return 注册是否成功
     */
    @PutMapping("/user")
    public Result<User> register(@Valid @RequestBody Param param){
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        User user = converter.toEntity(param);
        try {
            checkArgument(phoneCodeCache.checkCode(param.phone, param.code), FAILED_USER_CODE_NOT_AVAILABLE);
            checkArgument(dao.notExists(user), FAILED_USER_INFO_DUPLICATION);

            if(StringUtils.isNoneBlank(user.getPassword())) {
                user.setSalt(createSalt());
                user.setPassword(service.encryptPassword(user));
                user.setState(UserStateEnum.STATUS_NORMAL.getCode());

            }

            saveUser(user);

            transactionManager.commit(transaction);
            return success(user);
        } catch (Exception e) {
            e.printStackTrace();
            transactionManager.rollback(transaction);
            return failed(e.getMessage());
        }
    }

    private Integer createSalt() {
        return (new Random().nextInt(5) + 7) * 9;
    }

    private void saveUser(User user) throws DatabaseException {
        if(!save(user)) throw new DatabaseException("保存用户信息失败");
    }
}
