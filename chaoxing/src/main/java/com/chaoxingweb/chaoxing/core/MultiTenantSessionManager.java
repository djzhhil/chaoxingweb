package com.chaoxingweb.chaoxing.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 多租户会话管理器 - 支持多用户并发
 *
 * @author 小克 🐕💎
 * @since 2026-04-09
 */
@Slf4j
@Component
public class MultiTenantSessionManager {

    /**
     * 用户ID -> 会话上下文的映射
     * Key: userId 或 username
     * Value: SessionContext
     */
    private final Map<String, SessionContext> sessionMap = new ConcurrentHashMap<>();

    /**
     * 获取或创建用户的会话上下文
     *
     * @param userId 用户标识
     * @return 会话上下文
     */
    public SessionContext getOrCreateSession(String userId) {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        return sessionMap.compute(userId, (key, existingContext) -> {
            // 如果会话不存在或已过期，创建新会话
            if (existingContext == null || existingContext.isExpired()) {
                if (existingContext != null) {
                    log.info("用户会话已过期，创建新会话: userId={}", userId);
                } else {
                    log.debug("为用户创建新会话: userId={}", userId);
                }
                return new SessionContext(userId);
            }
            
            // 刷新会话活跃时间
            existingContext.refresh();
            return existingContext;
        });
    }

    /**
     * 获取用户的会话上下文（如果不存在返回 null）
     *
     * @param userId 用户标识
     * @return 会话上下文，不存在则返回 null
     */
    public SessionContext getSession(String userId) {
        if (userId == null || userId.isEmpty()) {
            return null;
        }

        SessionContext context = sessionMap.get(userId);
        if (context != null && !context.isExpired()) {
            context.refresh();
            return context;
        }

        // 会话不存在或已过期，移除
        if (context != null) {
            sessionMap.remove(userId);
            log.debug("移除过期会话: userId={}", userId);
        }

        return null;
    }

    /**
     * 更新用户的 Cookie
     *
     * @param userId 用户标识
     * @param cookie Cookie
     */
    public void updateCookie(String userId, String cookie) {
        SessionContext context = getOrCreateSession(userId);
        context.setCookie(cookie);
        log.debug("更新用户Cookie: userId={}", userId);
    }

    /**
     * 获取用户的 Cookie
     *
     * @param userId 用户标识
     * @return Cookie
     */
    public String getCookie(String userId) {
        SessionContext context = getSession(userId);
        return context != null ? context.getCookie() : null;
    }

    /**
     * 设置用户的 fid
     *
     * @param userId 用户标识
     * @param fid fid
     */
    public void setFid(String userId, String fid) {
        SessionContext context = getOrCreateSession(userId);
        context.setFid(fid);
    }

    /**
     * 获取用户的 fid
     *
     * @param userId 用户标识
     * @return fid
     */
    public String getFid(String userId) {
        SessionContext context = getSession(userId);
        return context != null ? context.getFid() : null;
    }

    /**
     * 设置用户的 uid
     *
     * @param userId 用户标识
     * @param uid uid
     */
    public void setUid(String userId, String uid) {
        SessionContext context = getOrCreateSession(userId);
        context.setUid(uid);
    }

    /**
     * 获取用户的 uid
     *
     * @param userId 用户标识
     * @return uid
     */
    public String getUid(String userId) {
        SessionContext context = getSession(userId);
        return context != null ? context.getUid() : null;
    }

    /**
     * 获取用户的请求头
     *
     * @param userId 用户标识
     * @return 请求头
     */
    public Map<String, String> getHeaders(String userId) {
        SessionContext context = getOrCreateSession(userId);
        return context.getHeaders();
    }

    /**
     * 更新用户的请求头
     *
     * @param userId 用户标识
     * @param headers 请求头
     */
    public void updateHeaders(String userId, Map<String, String> headers) {
        SessionContext context = getOrCreateSession(userId);
        context.getHeaders().putAll(headers);
    }

    /**
     * 关闭用户的会话
     *
     * @param userId 用户标识
     */
    public void closeSession(String userId) {
        SessionContext removed = sessionMap.remove(userId);
        if (removed != null) {
            log.info("关闭用户会话: userId={}", userId);
        }
    }

    /**
     * 清理所有过期会话
     *
     * @return 清理的会话数量
     */
    public int cleanExpiredSessions() {
        int count = 0;
        for (Map.Entry<String, SessionContext> entry : sessionMap.entrySet()) {
            if (entry.getValue().isExpired()) {
                sessionMap.remove(entry.getKey());
                count++;
                log.debug("清理过期会话: userId={}", entry.getKey());
            }
        }
        
        if (count > 0) {
            log.info("清理过期会话完成，共清理{}个会话", count);
        }
        
        return count;
    }

    /**
     * 获取当前活跃会话数量
     *
     * @return 会话数量
     */
    public int getActiveSessionCount() {
        return sessionMap.size();
    }
}
