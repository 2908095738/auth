package com.bbs.chat.app.chat.session;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Session 管理
 */
public class SessionManage {

    /**
     * 每个客户端的 WebSocketClient
     * ps: concurrent包的线程安全Set，用来存放每个客户端对应的WebSocketServer对象。
     */
    private static final ConcurrentHashMap<Long, WebSocketSession> map = new ConcurrentHashMap<>();

    public static Boolean exists(Long uid) {
        return map.containsKey(uid);
    }

    public static Boolean notExists(Long uid) {
        return !exists(uid);
    }

    public static void add(Long uid, WebSocketSession session) {
        map.put(uid, session);
    }

    public static void remove(Long uid) {
        map.remove(uid);
    }

    public static WebSocketSession search(Long uid) {
        return map.get(uid);
    }

    public static Boolean isNotEmpty() {
        return map.size() > NumberUtils.INTEGER_ZERO;
    }
}
