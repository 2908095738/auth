package com.bbs.chat.util;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;

public class StringUtil {

    /**
     * 超过 maxSize 的部分用省略号代替
     * <p>
     * 使用范例：
     * 1 不超过取所有
     * StringUtil.abbreviate("123456789", 11) = "123456789"
     * <p>
     * 2 超过最大长度截取并补充省略号
     * StringUtil.abbreviate("123456789", 3) = "123..."
     * <p>
     * 3 emoji表情被截断则丢弃前面的字符（整个表情）
     * StringUtil.abbreviate("123456789??", 10) = "123456789..."
     *
     * @param originStr 原始字符串
     * @param maxSize   最大长度
     */
    public static String abbreviate(String originStr, int maxSize) {

        return abbreviate(originStr, maxSize, null);
    }

    /**
     * 超过 maxSize 的部分用省略号代替
     * <p>
     * 使用范例：
     * <p>
     * StringUtil.abbreviate("123456789"", 3, "***") = "123..."
     *
     * @param originStr    原始字符串
     * @param maxSize      最大长度
     * @param abbrevMarker 省略符
     */
    public static String abbreviate(String originStr, int maxSize, String abbrevMarker) {

        Preconditions.checkArgument(maxSize > 0, "size 必须大于0");

        if (StringUtils.isEmpty(originStr)) {
            return StringUtils.EMPTY;
        }

        String defaultAbbrevMarker = "...";

        if (originStr.length() < maxSize) {
            return originStr;
        }

        // 截取前maxSize 个字符
        String head = originStr.substring(0, maxSize);

        // 最后一个字符是高代理项，则移除掉
        char lastChar = head.charAt(head.length() - 1);
        if (Character.isHighSurrogate(lastChar)) {
            head = head.substring(0, head.length() - 1);
        }


        return head + StringUtils.defaultIfEmpty(abbrevMarker, defaultAbbrevMarker);
    }
}