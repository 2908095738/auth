package com.clinic.util.log;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.clinic.entity.OperationLog;
import org.slf4j.event.Level;

public class LogUtil {

    /**
     * 操作日志
     * <p>
     *     注意：操作需要视为事务的一部分！！！！！（如果一个重要操作的执行，没有痕迹，则无法溯源可能发生的问题）
     * </p>
     */
    public static class Operation {

        /**
         * 记录
         * @param service   业务
         * @param operation 操作
         * @param level     级别
         */
        public static void record(Integer serviceCode, String service, String operation, Level level) {
            Db.save(new OperationLog(getExecLocation(), serviceCode, service, operation, level));
        }

        public static void record(ServiceLogEnums serviceLogEnum, Level level, CharSequence template, Object... params) {
            record(serviceLogEnum.getServiceCode(), serviceLogEnum.getServiceName(), StrUtil.format(template, params), level);
        }

        public static void recordStockInfoLog(CharSequence template, Object... params) {
            record(ServiceLogEnums.STOCK, Level.INFO, StrUtil.format(template, params), Level.INFO);
        }

        public static void recordRetailInfoLog(CharSequence template, Object... params) {
            record(ServiceLogEnums.RETAIL, Level.INFO, StrUtil.format(template, params), Level.INFO);
        }

        public static void recordAdmissionInfoLog(CharSequence template, Object... params) {
            record(ServiceLogEnums.ADMISSION, Level.INFO, StrUtil.format(template, params), Level.INFO);
        }

        public static void recordPayInfoLog(CharSequence template, Object... params) {
            record(ServiceLogEnums.PAY, Level.INFO, StrUtil.format(template, params), Level.INFO);
        }

        public static void recordPrescriptionInfoLog(CharSequence template, Object... params) {
            record(ServiceLogEnums.PRESCRIPTION, Level.INFO, StrUtil.format(template, params), Level.INFO);
        }
        public static void recordDiagnosisProofInfoLog(CharSequence template, Object... params) {
            record(ServiceLogEnums.DIAGNOSIS_PROOF, Level.INFO, StrUtil.format(template, params), Level.INFO);
        }

        public static void recordDisinfectionInfoLog(CharSequence template, Object... params) {
            record(ServiceLogEnums.DISINFECTION, Level.INFO, StrUtil.format(template, params), Level.INFO);
        }

        public static void recordDossierInfoLog(CharSequence template, Object... params) {
            record(ServiceLogEnums.DOSSIER, Level.INFO, StrUtil.format(template, params), Level.INFO);
        }

        public static void recordPatientInfoLog(CharSequence template, Object... params) {
            record(ServiceLogEnums.PATIENT, Level.INFO, StrUtil.format(template, params), Level.INFO);
        }

        public static void recordUserSettingInfoLog(CharSequence template, Object... params) {
            record(ServiceLogEnums.USER_SETTING, Level.INFO, StrUtil.format(template, params), Level.INFO);
        }

        public static void recordSterilizeInfoLog(CharSequence template, Object... params) {
            record(ServiceLogEnums.STERILIZE, Level.INFO, StrUtil.format(template, params), Level.INFO);
        }

        /**
         * 获取调用地址
         * @return 调用地址（格式：packagePath::methodName）
         */
        private static String getExecLocation() {
            StackTraceElement prevMethod = Thread.currentThread().getStackTrace()[3];
            return prevMethod.getClassName() + "::" + prevMethod.getMethodName();
        }
    }
}
