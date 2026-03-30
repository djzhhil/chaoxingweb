package com.chaoxingweb.chaoxing.auth.impl;

import com.chaoxingweb.chaoxing.auth.LoginService;
import com.chaoxingweb.chaoxing.core.AccountManager;
import com.chaoxingweb.chaoxing.core.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 登录服务实现
 *
 * @author 小克 🐕💎
 * @since 2026-03-30
 */
@Service
public class LoginServiceImpl implements LoginService {

    private final AccountManager accountManager;
    private final SessionManager sessionManager;

    @Autowired
    public LoginServiceImpl(AccountManager accountManager, SessionManager sessionManager) {
        this.accountManager = accountManager;
        this.sessionManager = sessionManager;
    }

    @Override
    public LoginResult loginWithPassword(String username, String password) {
        // 验证账号
        if (!accountManager.validateAccount(username, password)) {
            return new LoginResult(false, "用户名或密码错误");
        }

        // 更新账号状态
        accountManager.updateAccountStatus(username, AccountManager.AccountStatus.ACTIVE);

        // 生成 Token
        String token = generateToken(username);

        // 更新会话
        sessionManager.updateCookie(token);

        return new LoginResult(true, "登录成功", token, token);
    }

    @Override
    public LoginResult loginWithCookie(String cookie) {
        if (cookie == null || cookie.isEmpty()) {
            return new LoginResult(false, "Cookie 不能为空");
        }

        // 更新会话
        sessionManager.updateCookie(cookie);

        return new LoginResult(true, "Cookie 登录成功", null, cookie);
    }

    @Override
    public boolean validateLogin() {
        String cookie = sessionManager.getCookie();
        return cookie != null && !cookie.isEmpty();
    }

    @Override
    public void logout() {
        sessionManager.close();
    }

    /**
     * 生成 Token
     *
     * @param username 用户名
     * @return Token
     */
    private String generateToken(String username) {
        // 简单实现：使用用户名 + 时间戳
        long timestamp = System.currentTimeMillis();
        return username + ":" + timestamp;
    }
}
