package com.chaoxingweb.chaoxing.core.impl;

import com.chaoxingweb.chaoxing.core.SessionManager;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 会话管理器实现
 *
 * @author 小克 🐕💎
 * @since 2026-03-30
 */
@Component
public class SessionManagerImpl implements SessionManager {

    private String sessionId;
    private String cookie;
    private String fid;
    private String uid;
    private Map<String, String> headers;

    public SessionManagerImpl() {
        this.sessionId = UUID.randomUUID().toString();
        this.headers = new HashMap<>();
        this.headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public String getCookie() {
        return cookie;
    }

    @Override
    public void updateCookie(String cookie) {
        this.cookie = cookie;
    }

    @Override
    public String getFid() {
        return fid;
    }

    @Override
    public String getUid() {
        return uid;
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public void updateHeaders(Map<String, String> headers) {
        this.headers.putAll(headers);
    }

    public void setFid(String fid) {
        this.fid = fid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    public void close() {
        this.cookie = null;
        this.fid = null;
        this.uid = null;
        this.headers.clear();
    }
}
