package com.auth.user;

import com.auth.user.dto.UserDTO;
import com.auth.user.exception.UserNotLoginException;
import com.auth.user.impl.config.threadlocal.LoginUserThreadLocal;

import java.util.List;
import java.util.Objects;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ONE;

public interface User {

    interface Save {

        void save(UserDTO userDTO);
    }

    interface Edit {

        void updateAvatar(Long userId, String avatar);

        void bindVXOpenId(Long uid, String openId);
    }

    interface Search {
        UserDTO byPhone(String phone);

        UserDTO byId(Long id);

        List<UserDTO> byIds(List<Long> id);

        UserDTO byOpenId(String openId);
    }

    class LoginUserUtil {

        public static UserDTO tryGet() {
            return LoginUserThreadLocal.get();
        }

        public static Long tryGetId() {
            UserDTO userDTO = LoginUserThreadLocal.get();
            return nonNull(userDTO) ? userDTO.getId() : null;
        }

        public static Boolean loginUserIsAdmin() throws UserNotLoginException {
            UserDTO user = tryGet();
            if(isNull(user)) throw new UserNotLoginException();
            return Objects.equals(INTEGER_ONE, user.getIsAdmin());
        }

        public static Boolean loginUserNotIsAdmin() throws UserNotLoginException {
            return !(loginUserIsAdmin());
        }
    }
}
