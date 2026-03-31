package com.chaoxingweb.auth.util;

/**
 * Cookie 验证器接口
 *
 * @author 小克 🐕💎
 * @since 2026-03-30
 */
public interface CookieValidator {

    /**
     * 验证 Cookie 有效性
     *
     * @param cookie Cookie
     * @return 是否有效
     */
    boolean validateCookie(String cookie);

    /**
     * 刷新 Cookie
     *
     * @param cookie Cookie
     * @return 新的 Cookie
     */
    String refreshCookie(String cookie);

    /**
     * 检查 Cookie 过期
     *
     * @param cookie Cookie
     * @return 是否过期
     */
    boolean isCookieExpired(String cookie);

    /**
     * 从 Cookie 中提取用户名
     *
     * @param cookie Cookie
     * @return 用户名
     */
    String extractUsername(String cookie);
}
