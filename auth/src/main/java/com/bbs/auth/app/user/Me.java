package com.bbs.auth.app.user;

import com.bbs.Result;
import com.bbs.auth.converter.UserConverter;
import com.bbs.auth.entity.UserCompany;
import com.bbs.auth.service.CompanyService;
import com.bbs.auth.service.TokenService;
import com.bbs.auth.service.UserCompanyService;
import com.bbs.auth.service.UserService;
import com.bbs.exception.ReLoginException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.util.List;

import static com.bbs.Result.success;

@RestController
@RequestMapping
public class Me {

    @Resource
    private TokenService tokenService;

    @Resource
    private UserService service;

    @Resource
    private UserConverter converter;

    @Resource
    private CompanyService companyService;

    @Resource
    private HttpServletRequest request;

    /**
     * 当前用户个人信息
     */
    @GetMapping
    public Result<VO> me() throws ReLoginException {
        String token = tokenService.getToken(request);
        Long uid = tokenService.verify(token).getId();
        VO vo = converter.toMeVO(service.search(uid));
        List<UserCompany> userCompanyList = companyService.searchCompany(uid);
        vo.setUserCompanyList(userCompanyList);
        return success(vo);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VO {

        private Long id;

        private String name;

        private String email;

        private String phone;

        /**
         * 个性签名
         */
        private String sign;

        /**
         * 账号状态
         */
        private Integer state;

        /**
         * 图片 URL 地址
         */
        private String avatar;

        /**
         * 平台角色
         */
        private Integer paasRole;

        /**
         * 用户公司
         */
        List<UserCompany> userCompanyList;
    }
}
