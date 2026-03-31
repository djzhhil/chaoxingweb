package com.chaoxingweb.auth.service;

/**
 * 登录服务接口
 *
 * @author 小克 🐕💎
 * @since 2026-03-30
 */
public interface LoginService {

    /**
     * 账号密码登录
     *
     * @param username 用户名
     * @param password 密码
     * @return 登录结果
     */
    LoginResult loginWithPassword(String username, String password);

    /**
     * Cookie 登录
     *
     * @param cookie Cookie
     * @return 登录结果
     */
    LoginResult loginWithCookie(String cookie);

    /**
     * 验证登录状态
     *
     * @return 是否已登录
     */
    boolean validateLogin();

    /**
     * 登出
     */
    void logout();

    /**
     * 登录结果
     */
    class LoginResult {
        private boolean success;
        private String message;
        private String token;
        private String cookie;

        public LoginResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public LoginResult(boolean success, String message, String token, String cookie) {
            this.success = success;
            this.message = message;
            this.token = token;
            this.cookie = cookie;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getToken() {
            return token;
        }

        public String getCookie() {
            return cookie;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public void setCookie(String cookie) {
            this.cookie = cookie;
        }
    }
}
