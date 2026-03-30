package com.chaoxingweb.chaoxing.core;

/**
 * 账号管理器接口
 *
 * @author 小克 🐕💎
 * @since 2026-03-30
 */
public interface AccountManager {

    /**
     * 创建账号
     *
     * @param username 用户名
     * @param password 密码
     * @return 账号信息
     */
    Account createAccount(String username, String password);

    /**
     * 验证账号
     *
     * @param username 用户名
     * @param password 密码
     * @return 是否有效
     */
    boolean validateAccount(String username, String password);

    /**
     * 获取账号状态
     *
     * @param username 用户名
     * @return 账号状态
     */
    AccountStatus getAccountStatus(String username);

    /**
     * 更新账号状态
     *
     * @param username 用户名
     * @param status   账号状态
     */
    void updateAccountStatus(String username, AccountStatus status);

    /**
     * 账号信息
     */
    class Account {
        private String username;
        private String password;
        private AccountStatus status;
        private String lastLogin;

        public Account(String username, String password) {
            this.username = username;
            this.password = password;
            this.status = AccountStatus.INACTIVE;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public AccountStatus getStatus() {
            return status;
        }

        public void setStatus(AccountStatus status) {
            this.status = status;
        }

        public String getLastLogin() {
            return lastLogin;
        }

        public void setLastLogin(String lastLogin) {
            this.lastLogin = lastLogin;
        }
    }

    /**
     * 账号状态枚举
     */
    enum AccountStatus {
        /**
         * 未激活
         */
        INACTIVE,

        /**
         * 已激活
         */
        ACTIVE,

        /**
         * 已锁定
         */
        LOCKED,

        /**
         * 已过期
         */
        EXPIRED
    }
}
