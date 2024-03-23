package com.auth.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.jwt.JWTUtil;
import com.auth.entity.User;
import com.auth.entity.VXUser;
import com.auth.service.TokenService;
import com.bbs.enums.CodeEnum;
import com.bbs.exception.ReLoginException;
import com.auth.entity.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static cn.hutool.core.bean.BeanUtil.toBean;
import static com.auth.api.vx.VXLoginAuthAPI.OPEN_ID_KEY;
import static com.auth.api.vx.VXLoginAuthAPI.SESSION_KEY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Service
public class TokenServiceImpl implements TokenService {


    @Value("${jwt.token}")
    private String key;

    //token过期时间阈值
    @Value("${jwt.expireTime}")
    private Integer expireTime;

    @Value("${jwt.name}")
    private String tokenName;

    @Resource(name = "protoStuffTemplate")
    private RedisTemplate<String, String> redisTemplate;

    public static final String LOGIN_TOKEN_PREFIX = "AUTH_LOGIN_TOKEN_";

    /**
     * JWTUtil生成token
     * @param user 入参
     * @return token
     */
    @Override
    public String createToken(User user) {
        return JWTUtil.createToken(createJWTTokeParam(user), key.getBytes());
    }

    private Map<String, Object> createJWTTokeParam(User user) {
        Map<String, Object> map = new HashMap<>();
        Date date = DateUtil.parse(DateUtil.now());
        Date failureTokenTime = DateUtil.offsetDay(date, + expireTime);
        map.put("id", user.getId());
        map.put("name",user.getName());
        map.put("failureTokenTime",failureTokenTime.getTime());
        return map;
    }

    @Override
    public String createToken(VXUser user) {
        Map<String, Object> param = createJWTTokeParam(user);
        param.put(OPEN_ID_KEY, user.getOpenid());
        param.put(SESSION_KEY, user.getSession_key());
        return JWTUtil.createToken(param, key.getBytes());
    }

    @Override
    public void setLoginFlag(Long uid) {
        redisTemplate.opsForValue().set(getTokenKey(uid), DateUtil.now(), expireTime, TimeUnit.DAYS);
    }


    @Override
    public String getLoginFlag(Long uid) {
        return redisTemplate.opsForValue().get(getTokenKey(uid));
    }

    @Override
    public void clearLoginFlag(Long uid) {
        redisTemplate.delete(getTokenKey(uid));
    }

    @Override
    public String getToken(HttpServletRequest request) {
        return request.getHeader(tokenName);
    }

    @Override
    public String getTokenKey(Long uid) {
        return LOGIN_TOKEN_PREFIX + uid;
    }

    @Override
    public UserVO verify(String token) throws ReLoginException {
        if(verifyToken(token)) {
            UserVO user = parseToken(token);
            if(isNotBlank(redisTemplate.opsForValue().get(getTokenKey(user.getId())))) {
                return user;
            }
        }
        throw new ReLoginException(CodeEnum.FAILED_USER_INFO_DUPLICATION);
    }

    @Override
    public UserVO parseToken(String token) {
        return toBean(getPayloads(token), UserVO.class);
    }


    /**
     * 验证 token 且正确
     * @param token Auth 服务颁发的登录 Token（Login 接口获取）
     * @return Token 能否解析成功
     */
    @Override
    public Boolean verifyToken(String token) {
        if(isNotBlank(token))
            return JWTUtil.verify(token, key.getBytes());
        return false;
    }

    /**
     * 更新redis中登录时间
     */
    public void extendLoginTime(UserVO user) {
        setLoginFlag(user.getId());
    }

    @Override
    public void extendLoginTime(User user) {
        setLoginFlag(user.getId());
    }

    /**
     * JWT解析token返回用户信息
     * @param token token
     * @return 用户信息
     */
    public JSONObject getPayloads(String token) {
        return JWTUtil.parseToken(token).getPayloads();
    }
}
