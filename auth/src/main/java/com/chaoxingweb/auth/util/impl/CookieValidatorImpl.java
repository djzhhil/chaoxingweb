package com.chaoxingweb.auth.util.impl;

import com.chaoxingweb.auth.util.CookieValidator;
import org.springframework.stereotype.Component;

/**
 * Cookie 验证器实现
 *
 * @author 小克 🐕💎
 * @since 2026-03-30
 */
@Component
public class CookieValidatorImpl implements CookieValidator {

    private static final long COOKIE_EXPIRY_TIME = 7 * 24 * 60 * 60 * 1000; // 7 天

    @Override
    public boolean validateCookie(String cookie) {
        if (cookie == null || cookie.isEmpty()) {
            return false;
        }

        // 检查 Cookie 格式
        if (!cookie.contains(":")) {
            return false;
        }

        // 检查是否过期
        if (isCookieExpired(cookie)) {
            return false;
        }

        return true;
    }

    @Override
    public String refreshCookie(String cookie) {
        if (!validateCookie(cookie)) {
            return null;
        }

        String username = extractUsername(cookie);
        long timestamp = System.currentTimeMillis();

        return username + ":" + timestamp;
    }

    @Override
    public boolean isCookieExpired(String cookie) {
        if (cookie == null || !cookie.contains(":")) {
            return true;
        }

        try {
            String[] parts = cookie.split(":");
            if (parts.length < 2) {
                return true;
            }

            long timestamp = Long.parseLong(parts[1]);
            long currentTime = System.currentTimeMillis();

            return (currentTime - timestamp) > COOKIE_EXPIRY_TIME;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    @Override
    public String extractUsername(String cookie) {
        if (cookie == null || !cookie.contains(":")) {
            return null;
        }

        String[] parts = cookie.split(":");
        if (parts.length > 0) {
            return parts[0];
        }

        return null;
    }
}
