package com.bbs.auth.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.jwt.JWTUtil;
import com.bbs.auth.cache.TokenCache;
import com.bbs.auth.cache.user.UserCache;
import com.bbs.auth.entity.User;
import com.bbs.auth.service.TokenService;
import com.bbs.enums.CodeEnum;
import com.bbs.exception.ReLoginException;
import com.bbs.entity.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
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
import static java.util.Objects.nonNull;
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

    @Resource
    private TokenCache cache;

    @Resource
    private UserCache userCache;

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
        String token = request.getHeader(tokenName);
        return StringUtils.isNotBlank(token) ? token.split(" ")[1] : null;
    }

    @Override
    public String verifyAndExpireToken(User user) {
        String token = cache.getToken(user.getId());
        if(Strings.isNotBlank(token)) {
            UserVO vo = verify(token);
            cache.expireToken(vo.getId());
        } else {
            token = createToken(user);
            cache.setToken(user.getId(), token);
        }
        return token;
    }

    @Override
    public String getTokenKey(Long uid) {
        return LOGIN_TOKEN_PREFIX + uid;
    }

    @Override
    public UserVO verify(String token) throws ReLoginException {
        if(verifyToken(token)) {
            Long id = parseToken(token).getId();
            if(nonNull(getLoginFlag(id))) {
                try {
                    User user = userCache.search(id);
                    return new UserVO(user.getId(), user.getName(), user.getEmail(), user.getPhone().toString());
                } catch (InterruptedException e) {
                    throw new ReLoginException(CodeEnum.FAILED_USER_INFO_DUPLICATION);
                }
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
        if(isNotBlank(token)) {
            return JWTUtil.verify(token, key.getBytes());
        }
        return false;
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
