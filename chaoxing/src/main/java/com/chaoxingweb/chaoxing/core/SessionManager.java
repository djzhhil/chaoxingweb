package com.chaoxingweb.chaoxing.core;

import java.util.Map;

/**
 * 会话管理器接口
 *
 * @author 小克 🐕💎
 * @since 2026-03-30
 */
public interface SessionManager {

    /**
     * 获取会话 ID
     *
     * @return 会话 ID
     */
    String getSessionId();

    /**
     * 获取 Cookie
     *
     * @return Cookie
     */
    String getCookie();

    /**
     * 更新 Cookie
     *
     * @param cookie Cookie
     */
    void updateCookie(String cookie);

    /**
     * 获取 fid
     *
     * @return fid
     */
    String getFid();

    /**
     * 获取 uid
     *
     * @return uid
     */
    String getUid();

    /**
     * 获取请求头
     *
     * @return 请求头
     */
    Map<String, String> getHeaders();

    /**
     * 更新请求头
     *
     * @param headers 请求头
     */
    void updateHeaders(Map<String, String> headers);

    /**
     * 关闭会话
     */
    void close();
}
