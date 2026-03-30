package com.chaoxingweb.chaoxing.core.impl;

import com.chaoxingweb.chaoxing.core.AccountManager;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 账号管理器实现
 *
 * @author 小克 🐕💎
 * @since 2026-03-30
 */
@Component
public class AccountManagerImpl implements AccountManager {

    private final Map<String, Account> accountMap = new HashMap<>();

    @Override
    public Account createAccount(String username, String password) {
        Account account = new Account(username, password);
        accountMap.put(username, account);
        return account;
    }

    @Override
    public boolean validateAccount(String username, String password) {
        Account account = accountMap.get(username);
        if (account == null) {
            return false;
        }
        return account.getPassword().equals(password);
    }

    @Override
    public AccountStatus getAccountStatus(String username) {
        Account account = accountMap.get(username);
        if (account == null) {
            return AccountStatus.INACTIVE;
        }
        return account.getStatus();
    }

    @Override
    public void updateAccountStatus(String username, AccountStatus status) {
        Account account = accountMap.get(username);
        if (account != null) {
            account.setStatus(status);
            account.setLastLogin(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
    }
}
