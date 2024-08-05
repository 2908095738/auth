package com.clinic.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.api.auth.User;
import com.bbs.exception.BusinessException;
import com.clinic.app.AppPrescriptionService;
import com.clinic.cache.log.admission.AdmissionLogCache;
import com.clinic.converter.DossierConverter;
import com.clinic.dao.DossierDao;
import com.clinic.dto.PrescriptionDto;
import com.clinic.dto.param.SaveDossierParam;
import com.clinic.dto.param.UpdateDossierParam;
import com.clinic.entity.AdmissionLog;
import com.clinic.entity.Dossier;
import com.clinic.entity.DossierPrescription;
import com.clinic.mapper.DossierMapper;
import com.clinic.service.AdmissionLogService;
import com.clinic.service.DossierService;
import com.clinic.util.log.LogUtil;
import com.clinic.util.LoginUser;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.google.common.base.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

/**
 * 病历
 */
@Service
public class DossierServiceImpl extends MPJBaseServiceImpl<DossierMapper, Dossier>
    implements DossierService{

    private DossierDao dao;

    private DossierConverter converter;

    private AppPrescriptionService appPrescriptionService;

    private AdmissionLogService admissionLogService;

    @Resource
    private AdmissionLogCache admissionLogCache;

    @Resource
    private DataSourceTransactionManager transactionManager;

    @Resource
    private TransactionDefinition transactionDefinition;

    @Override
    public Result<Long> createDossier(SaveDossierParam param) throws BusinessException {
        User user = LoginUser.get();
        Dossier dossier = converter.toEntity(param);
        dossier.setUserId(user.getId());
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            if(!save(dossier)) throw new BusinessException();
            LogUtil.Operation.addDossier(param.getPatientId(), dossier.getId(), "{}添加病例：病例id={}, 门诊日志id={}", user.getName(), dossier.getId(), param.getAdmissionID());
            transactionManager.commit(transaction);
            return Result.success(dossier.getId());
        } catch (Exception e) {
            transactionManager.rollback(transaction);
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    @Override
    public Result<Long> updateDossier(UpdateDossierParam param) throws BusinessException {
        Dossier dossier = converter.toEntity(param);
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            Dossier dossierDB = getById(param.getId());
            Preconditions.checkArgument(nonNull(dossierDB), "病例不存在，可能已删除");
            if(!updateById(dossier)) throw new BusinessException("更新病历失败！");
            admissionLogService.lambdaUpdate()
                    .set(AdmissionLog::getDiagnosis, dossier.getDiagnosis())
                    .eq(AdmissionLog::getDossierId, dossier.getId())
                    .update();
            admissionLogCache.remove();
            LogUtil.Operation.updateDossier(dossierDB.getPatientId(), dossierDB.getId(), "{}修改病例：病例id={}", LoginUser.get().getName(), param.getId());
            transactionManager.commit(transaction);
            return Result.success(dossier.getId());
        } catch (Exception e) {
            transactionManager.rollback(transaction);
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public Result<Page<Dossier>> select(Long userId, String id, Integer current, Integer size) {
        return Result.success(selectPatientDossier(userId, id, current, size));
    }

    @Override
    public Dossier getDossierByPrescriptionId(Long prescriptionId) {
        return baseMapper.selectJoinOne(Dossier.class, new MPJLambdaWrapper<Dossier>()
                .selectAll(Dossier.class)
                .leftJoin(DossierPrescription.class,DossierPrescription::getDossierId,Dossier::getId)
                .eq(DossierPrescription::getPrescriptionId,prescriptionId)
        );
    }

    private Page<Dossier> selectPatientDossier(Long userId,String patientId,Integer pageNo,Integer pageSize) {
        Page<Dossier> dossierPage = dao.selectedById(userId, patientId, pageNo, pageSize);
        List<Dossier> dossiers = dossierPage.getRecords();
        if (CollectionUtil.isNotEmpty(dossiers)){
            List<Long> idList = dossiers.stream().map(Dossier::getId).collect(Collectors.toList());
            List<PrescriptionDto> prescriptionParams = appPrescriptionService.selectByIds(userId, idList);
            if(CollectionUtil.isNotEmpty(prescriptionParams)){
                Map<Long, PrescriptionDto> prescriptionMap = prescriptionParams.stream().collect(Collectors.toMap(PrescriptionDto::getDossierId, o1 -> o1));
                for (Dossier dossier : dossierPage.getRecords()) {
                    dossier.setPrescriptionDto(prescriptionMap.get(dossier.getId()));
                }
            }
        }
        return dossierPage;
    }

    @Autowired
    public void setDao(DossierDao dao) {
        this.dao = dao;
    }
    @Autowired
    public void setConverter(DossierConverter converter) {
        this.converter = converter;
    }
    @Lazy
    @Autowired
    public void setAppPrescriptionService(AppPrescriptionService appPrescriptionService) {
        this.appPrescriptionService = appPrescriptionService;
    }

    @Autowired
    public void setAdmissionLogService(AdmissionLogService admissionLogService) {
        this.admissionLogService = admissionLogService;
    }
}




