package com.chaoxingweb.chaoxing.core;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 会话上下文 - 存储单个用户的会话信息
 *
 * @author 小克 🐕💎
 * @since 2026-04-09
 */
@Data
@NoArgsConstructor
public class SessionContext {

    /**
     * 会话 ID
     */
    private String sessionId;

    /**
     * Cookie
     */
    private String cookie;

    /**
     * fid
     */
    private String fid;

    /**
     * uid
     */
    private String uid;

    /**
     * 请求头
     */
    private Map<String, String> headers;

    /**
     * 最后访问时间
     */
    private LocalDateTime lastAccessTime;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    public SessionContext(String userId) {
        this.sessionId = UUID.randomUUID().toString();
        this.headers = new HashMap<>();
        this.headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
        this.lastAccessTime = LocalDateTime.now();
        // 默认30分钟过期
        this.expireTime = LocalDateTime.now().plusMinutes(30);
    }

    /**
     * 更新最后访问时间并刷新过期时间
     */
    public void refresh() {
        this.lastAccessTime = LocalDateTime.now();
        this.expireTime = LocalDateTime.now().plusMinutes(30);
    }

    /**
     * 检查会话是否过期
     *
     * @return true 如果已过期
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expireTime);
    }
}
