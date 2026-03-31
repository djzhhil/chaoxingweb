package com.chaoxingweb.auth.util.impl;

import com.chaoxingweb.auth.util.TokenManager;
import org.springframework.stereotype.Component;

/**
 * Token 管理器实现
 *
 * @author 小克 🐕💎
 * @since 2026-03-30
 */
@Component
public class TokenManagerImpl implements TokenManager {

    private static final long TOKEN_EXPIRY_TIME = 24 * 60 * 60 * 1000; // 24 小时

    @Override
    public String generateToken(String username) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }

        long timestamp = System.currentTimeMillis();
        return username + ":" + timestamp;
    }

    @Override
    public boolean validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        // 检查 Token 格式
        if (!token.contains(":")) {
            return false;
        }

        // 检查是否过期
        if (isTokenExpired(token)) {
            return false;
        }

        return true;
    }

    @Override
    public String refreshToken(String token) {
        if (!validateToken(token)) {
            return null;
        }

        String username = extractUsername(token);
        return generateToken(username);
    }

    @Override
    public String extractUsername(String token) {
        if (token == null || !token.contains(":")) {
            return null;
        }

        String[] parts = token.split(":");
        if (parts.length > 0) {
            return parts[0];
        }

        return null;
    }

    @Override
    public boolean isTokenExpired(String token) {
        if (token == null || !token.contains(":")) {
            return true;
        }

        try {
            String[] parts = token.split(":");
            if (parts.length < 2) {
                return true;
            }

            long timestamp = Long.parseLong(parts[1]);
            long currentTime = System.currentTimeMillis();

            return (currentTime - timestamp) > TOKEN_EXPIRY_TIME;
        } catch (NumberFormatException e) {
            return true;
        }
    }
}
