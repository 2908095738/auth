package com.bbs.chat.app.chat.api;

import java.util.concurrent.atomic.AtomicInteger;

public class OnlineCount {

    /**
     * 线连接数
     * ps: 需要线程安全！
     */
    public static final AtomicInteger count = new AtomicInteger();

    public static void incr() {
        count.incrementAndGet();
    }

    public static void decr() {
        count.decrementAndGet();
    }
}
