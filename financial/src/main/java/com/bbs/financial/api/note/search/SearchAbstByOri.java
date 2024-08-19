package com.bbs.financial.api.note.search;

import com.bbs.Result;
import com.bbs.financial.controller.ZhangHuController;
import com.bbs.financial.entity.CertificateAbstract;
import com.bbs.financial.entity.ZhangHu;
import com.bbs.financial.util.SpringUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 查询初始余额生成凭证的部分摘要
 */
@RestController
@RequestMapping
public class SearchAbstByOri {

    @Resource
    private ApplicationContext appContext;

    private class StringTip {
        public static final String ABST = "初始金额";

        public static final String NO_ZH = "未找到账户信息";
    }

    /**
     * 查询初始余额生成凭证的部分摘要
     *
     * @param zhId 账户id
     */
    @GetMapping("/note/ori/cert/{zhId}")
    public Result<CertificateAbstract> search(@PathVariable Long zhId) {
        ZhangHu zh = SpringUtil.getRespData(ZhangHuController.class, appContext, z -> z.getInfo(zhId));
        CertificateAbstract abst = new CertificateAbstract();
        abst.setCertificateAbstract(StringTip.ABST);
        abst.setAccountId(zh.getSubjectsId());

        if (Objects.nonNull(zh)) return Result.success(abst);
        else return Result.failed(StringTip.NO_ZH);
    }
}