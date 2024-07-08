package com.bbs.financial.util;

public final class SpringUtil {
    private SpringUtil() {
        throw new RuntimeException("no create obj");
    }

    /**
     * 获得首字母小写的类名称
     */
    public static String getClassNameOfFirstLow(Class clazz) {
        String className = clazz.getSimpleName();
        String firstEN = className.substring(0, 1);
        String otherEN = className.substring(1);
        return firstEN.toLowerCase() + otherEN;
    }
}