package com.chaoxingweb.course.service;

/**
 * 账号绑定服务接口
 *
 * @author 小克 🐕💎
 * @since 2026-03-31
 */
public interface AccountBindingService {

    /**
     * 绑定超星账号
     *
     * @param userId 用户 ID
     * @param username 超星用户名
     * @param password 超星密码
     * @param cookie 超星 Cookie
     * @param useCookie 是否使用 Cookie 登录
     */
    void bindChaoxingAccount(Long userId, String username, String password, String cookie, boolean useCookie);

    /**
     * 解绑超星账号
     *
     * @param userId 用户 ID
     */
    void unbindChaoxingAccount(Long userId);

    /**
     * 验证超星账号
     *
     * @param username 超星用户名
     * @param password 超星密码
     * @param cookie 超星 Cookie
     * @param useCookie 是否使用 Cookie 登录
     * @return 是否有效
     */
    boolean validateChaoxingAccount(String username, String password, String cookie, boolean useCookie);
}
