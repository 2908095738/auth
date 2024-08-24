package com.bbs.financial.api.certificate.delete;

import com.bbs.Result;
import com.bbs.financial.api.invoice.delete.DeleteInvoice;
import com.bbs.financial.api.note.delete.DeleteNote;
import com.bbs.financial.entity.CertificateAbstract;
import com.bbs.financial.enums.CertTypeEnum;
import com.bbs.financial.service.CertificateAbstractService;
import com.bbs.financial.service.CertificateService;
import com.bbs.financial.util.ORMUtil;
import com.bbs.financial.util.SpringUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 删除记账凭证
 */
@Controller
@RequestMapping("/certificate")
public class DelCertificate {
    @Resource
    private TransactionDefinition tranDef;
    @Resource
    private DataSourceTransactionManager tranManager;

    @Resource
    private ApplicationContext appContext;

    @Resource
    private CertificateService certORM;

    @Resource
    private CertificateAbstractService abstORM;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DelParam {

        /**
         * 凭证类型: {@link CertTypeEnum}
         */
        private Integer type;

        /**
         * 凭证id
         */
        private Long id;
    }

    /**
     * 删除记账凭证
     */
    @ResponseBody
    @DeleteMapping("/batch")
    public Result<Boolean> remove(@RequestBody List<DelParam> delList) {
        Map<CertTypeEnum, List<Long>> linkMap = getLinkDelMap(delList);

        List<Long> certIdList = delList.stream().map(DelParam::getId).collect(Collectors.toList());
        return ORMUtil.fastTran(() -> {
            //删除具体凭证相关
            boolean isDone = false;
            isDone = certORM.removeBatchByIds(certIdList);
            isDone = abstORM.lambdaUpdate()
                    .in(CertificateAbstract::getCertificateId, certIdList)
                    .remove();

            if (isDone)
                isDone = linkDel(linkMap);

            if (!isDone)
                throw new RuntimeException();
        }, tranManager, tranDef);
    }

    /**
     * 获取联动删除映射
     **/
    private Map<CertTypeEnum, List<Long>> getLinkDelMap(List<DelParam> delList) {
        Map<CertTypeEnum, List<Long>> linkMap = new HashMap<>();
        List<CertTypeEnum> typeList = Arrays.asList(CertTypeEnum.values());

        for (DelParam param : delList) {
            //初始化key
            CertTypeEnum key = null;
            CertTypeEnum typeEnum = CertTypeEnum.enumMap.get(param.getType());
            if (Objects.nonNull(typeEnum) && typeList.contains(typeEnum))
                key = typeEnum;

            //初始化或再赋值value
            linkMap.merge(key,
                    new ArrayList<Long>() {{
                        add(param.getId());
                    }},
                    (l, r) -> {
                        l.add(param.getId());
                        return l;
                    });
        }

        return linkMap;
    }

    /**
     * 联动删除
     */
    private boolean linkDel(Map<CertTypeEnum, List<Long>> linkMap) {
        Set<Map.Entry<CertTypeEnum, List<Long>>> entries = linkMap.entrySet();
        boolean isDone = false;
        for (Map.Entry<CertTypeEnum, List<Long>> nowTop : entries) {
            switch (nowTop.getKey()) {
                case BUY_ASSET:
                case OLD_ASSET:
                case LESS_ASSET:
                case CLEAR_ASSET:
                case OTHER_ASSET:
                    isDone = true;
                    break;
                case ORI_NOTE:
                    isDone = SpringUtil.getRespData(DeleteNote.class, appContext, d -> d.removeByCert(nowTop.getValue()));
                    break;
                case NORMAL_NOTE:
                    isDone = SpringUtil.getRespData(DeleteNote.class, appContext, d -> d.clearCert(nowTop.getValue()));
                    break;
                case INVOICE:
                    isDone = SpringUtil.getRespData(DeleteInvoice.class, appContext, d -> d.clearCert(nowTop.getValue()));
                    break;
                case NONE:
                    isDone = true;
                    break;
                default:
                    isDone = true;
                    break;
            }
        }

        return isDone;
    }
}