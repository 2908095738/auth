package com.auth.app.register;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.db.DbRuntimeException;
import com.auth.enums.ResourceNames;
import com.auth.service.ResourceService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bbs.Result;
import com.auth.converter.UserConverter;
import com.auth.dao.UserDao;
import com.auth.entity.User;
import com.auth.entity.UserGroup;
import com.auth.mapper.UserMapper;
import com.auth.service.UserGroupService;
import com.bbs.enums.UserStateEnum;
import lombok.Data;
import net.sf.jsqlparser.util.validation.metadata.DatabaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import java.util.Random;

import static com.bbs.Result.failed;
import static com.bbs.Result.success;
import static com.bbs.enums.CodeEnum.FAILED_USER_INFO_DUPLICATION;

@RestController
@RequestMapping
public class RegisterUser extends ServiceImpl<UserMapper, User> {

    private final DataSourceTransactionManager transactionManager;
    private final TransactionDefinition transactionDefinition;

    @Resource
    private UserConverter converter;
    private final UserDao dao;
    private final UserGroupService userGroupService;

    @Resource
    private ResourceService resourceService;

    @Data
    public static class UserRegisterParam {

        @NotBlank
        private String name;

        @NotBlank
        private String clinicName;

        @NotBlank
        private String password;

        private Long phone;

        @NotBlank
        private String email;

        @NotNull
        private Long group;
    }


    /**
     * 用户注册
     * @param param 注册用户入参
     * @return 注册是否成功
     */
    @PutMapping("/user")
    public Result<Boolean> register(@Valid @RequestBody UserRegisterParam param){
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        User user = converter.toEntity(param);
        try {
            if(dao.userIsExist(user)) return failed(FAILED_USER_INFO_DUPLICATION);

            user.setSalt(createSalt());
            user.setPassword(encryptPassword(user));
            user.setState(UserStateEnum.STATUS_NORMAL.getCode());

            saveUser(user);
            saveUserGroup(user, param);
            saveUserClinicNameConfig(user, param);

            transactionManager.commit(transaction);
            return success();
        } catch (Exception e) {
            e.printStackTrace();
            transactionManager.rollback(transaction);
            return failed(e.getMessage());
        }
    }

    private Integer createSalt() {
        return (new Random().nextInt(5) + 7) * 9;
    }

    private String encryptPassword(User user) {
        return encryptPassword(user.getPassword(), user.getSalt());
    }

    public String encryptPassword(String pwd, Integer salt) {
        return SecureUtil.md5(pwd + salt);
    }

    private void saveUser(User user) throws DatabaseException {
        if(!save(user)) throw new DatabaseException("保存用户信息失败");
    }

    private void saveUserGroup(User user, UserRegisterParam param) throws DatabaseException {
        if(!userGroupService.save(new UserGroup(user.getId(), param.getGroup()))) throw new DatabaseException("保存用户 & 组关联信息失败");
    }

    private void saveUserClinicNameConfig(User user, UserRegisterParam param) throws DbRuntimeException {
        resourceService.saveUserConfig(user.getId(), ResourceNames.UserConfig.CLINIC_NAME.getName(), param.clinicName, "保存用户诊所名称配置信息失败");
    }


    @Autowired
    public RegisterUser(DataSourceTransactionManager transactionManager, TransactionDefinition transactionDefinition, UserDao dao, UserGroupService userGroupService) {
        this.transactionManager = transactionManager;
        this.transactionDefinition = transactionDefinition;
        this.dao = dao;
        this.userGroupService = userGroupService;
    }
}
