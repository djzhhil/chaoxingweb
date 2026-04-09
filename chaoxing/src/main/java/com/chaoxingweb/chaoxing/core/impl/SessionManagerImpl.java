package com.chaoxingweb.chaoxing.core.impl;

import com.chaoxingweb.chaoxing.core.MultiTenantSessionManager;
import com.chaoxingweb.chaoxing.core.SessionContext;
import com.chaoxingweb.chaoxing.core.SessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 会话管理器实现 - 多租户兼容层
 * 
 * 从 SecurityContext 获取当前用户，委托给 MultiTenantSessionManager 管理
 *
 * @author 小克 🐕💎
 * @since 2026-03-30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SessionManagerImpl implements SessionManager {

    private final MultiTenantSessionManager multiTenantSessionManager;

    /**
     * 获取当前用户标识
     *
     * @return 用户ID或用户名
     */
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("用户未登录，使用默认会话");
            return "anonymous";
        }
        
        String username = authentication.getName();
        log.debug("当前用户: {}", username);
        return username;
    }

    /**
     * 获取当前用户的会话上下文
     *
     * @return 会话上下文
     */
    private SessionContext getCurrentSession() {
        String userId = getCurrentUserId();
        return multiTenantSessionManager.getOrCreateSession(userId);
    }

    @Override
    public String getSessionId() {
        return getCurrentSession().getSessionId();
    }

    @Override
    public String getCookie() {
        return getCurrentSession().getCookie();
    }

    @Override
    public void updateCookie(String cookie) {
        String userId = getCurrentUserId();
        multiTenantSessionManager.updateCookie(userId, cookie);
        log.info("更新用户会话Cookie: userId={}", userId);
    }

    @Override
    public String getFid() {
        return getCurrentSession().getFid();
    }

    @Override
    public String getUid() {
        return getCurrentSession().getUid();
    }

    @Override
    public void setFid(String fid) {
        String userId = getCurrentUserId();
        multiTenantSessionManager.setFid(userId, fid);
    }

    @Override
    public void setUid(String uid) {
        String userId = getCurrentUserId();
        multiTenantSessionManager.setUid(userId, uid);
    }

    @Override
    public Map<String, String> getHeaders() {
        return getCurrentSession().getHeaders();
    }

    @Override
    public void updateHeaders(Map<String, String> headers) {
        String userId = getCurrentUserId();
        multiTenantSessionManager.updateHeaders(userId, headers);
    }

    @Override
    public void close() {
        String userId = getCurrentUserId();
        multiTenantSessionManager.closeSession(userId);
        log.info("关闭用户会话: userId={}", userId);
    }
}
