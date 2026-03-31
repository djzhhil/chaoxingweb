package com.chaoxingweb.auth.util;

/**
 * Token 管理器接口
 *
 * @author 小克 🐕💎
 * @since 2026-03-30
 */
public interface TokenManager {

    /**
     * 生成 Token
     *
     * @param username 用户名
     * @return Token
     */
    String generateToken(String username);

    /**
     * 验证 Token
     *
     * @param token Token
     * @return 是否有效
     */
    boolean validateToken(String token);

    /**
     * 刷新 Token
     *
     * @param token Token
     * @return 新的 Token
     */
    String refreshToken(String token);

    /**
     * 从 Token 中提取用户名
     *
     * @param token Token
     * @return 用户名
     */
    String extractUsername(String token);

    /**
     * 检查 Token 过期
     *
     * @param token Token
     * @return 是否过期
     */
    boolean isTokenExpired(String token);
}
