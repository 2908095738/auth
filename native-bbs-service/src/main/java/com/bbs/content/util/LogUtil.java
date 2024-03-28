package com.bbs.content.util;

import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.bbs.content.entity.OperationLog;
import org.slf4j.event.Level;

public class LogUtil {

    /**
     * 操作日志
     * <p>
     *     注意：操作需要视为事务的一部分！（如果一个重要操作的执行，没有痕迹，则无法溯源可能发生的问题）
     * </p>
     */
    public static class Operation {

        /**
         * 记录
         * @param service 业务
         * @param operation 操作
         * @param level 级别
         * @return 记录结果
         */
        public static Boolean record(String service, String operation, Level level) {
            return Db.save(new OperationLog(getExecLocation(), service, operation, level));
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
