package com.chaoxingweb.chaoxing.auth.impl;

import com.chaoxingweb.chaoxing.auth.LoginService;
import com.chaoxingweb.chaoxing.core.AccountManager;
import com.chaoxingweb.chaoxing.core.CipherManager;
import com.chaoxingweb.chaoxing.core.SessionManager;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 登录服务实现
 *
 * 严格对照 Python 实现：
 * - 账号密码登录流程
 * - Cookie 登录流程
 * - Cookie 验证流程
 * - 保存 Cookie 流程
 *
 * @author 小克 🐕💎
 * @since 2026-03-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private static final String LOGIN_URL = "https://passport2.chaoxing.com/fanyalogin";
    private static final String COURSE_LIST_URL = "https://mooc2-ans.chaoxing.com/mooc2-ans/visit/courselistdata";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36";

    private final AccountManager accountManager;
    private final SessionManager sessionManager;
    private final CipherManager cipherManager;

    private OkHttpClient httpClient;

    @PostConstruct
    public void init() {
        // 创建 OkHttpClient
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .followRedirects(false)
                .followSslRedirects(true)
                .build();
    }

    @Override
    public LoginResult loginWithPassword(String username, String password) {
        log.info("开始账号密码登录: username={}", username);

        try {
            // 加密用户名和密码
            String encryptedUsername = cipherManager.encrypt(username);
            String encryptedPassword = cipherManager.encrypt(password);

            log.debug("加密后的用户名: {}", encryptedUsername);
            log.debug("加密后的密码: {}", encryptedPassword);

            // 构造登录数据（严格对照 Python）
            FormBody.Builder formBuilder = new FormBody.Builder()
                    .add("fid", "-1")
                    .add("uname", encryptedUsername)
                    .add("password", encryptedPassword)
                    .add("refer", "https%3A%2F%2Fi.chaoxing.com")
                    .add("t", "true")
                    .add("forbidotherlogin", "0")
                    .add("validate", "")
                    .add("doubleFactorLogin", "0")
                    .add("independentId", "0");

            // 构造请求
            Request request = new Request.Builder()
                    .url(LOGIN_URL)
                    .post(formBuilder.build())
                    .addHeader("User-Agent", USER_AGENT)
                    .addHeader("sec-ch-ua", "\"Chromium\";v=\"118\", \"Google Chrome\";v=\"118\", \"Not=A?Brand\";v=\"99\"")
                    .addHeader("sec-ch-ua-mobile", "?0")
                    .addHeader("sec-ch-ua-platform", "\"Windows\"")
                    .build();

            // 发送请求
            Response response = httpClient.newCall(request).execute();

            if (!response.isSuccessful()) {
                log.error("登录请求失败: status={}", response.code());
                return new LoginResult(false, "登录请求失败: " + response.code());
            }

            // 解析响应
            String responseBody = response.body().string();
            log.debug("登录响应: {}", responseBody);

            // 提取 Cookie
            String cookie = extractCookie(response);
            log.debug("提取的 Cookie: {}", cookie);

            // 更新会话
            sessionManager.updateCookie(cookie);

            // 简单判断登录是否成功（实际应该解析 JSON）
            if (responseBody.contains("\"status\":true") || responseBody.contains("\"status\": true")) {
                log.info("登录成功");
                return new LoginResult(true, "登录成功", null, cookie);
            } else {
                log.error("登录失败: {}", responseBody);
                return new LoginResult(false, "登录失败");
            }

        } catch (Exception e) {
            log.error("登录异常", e);
            return new LoginResult(false, "登录异常: " + e.getMessage());
        }
    }

    @Override
    public LoginResult loginWithCookie(String cookie) {
        log.info("开始 Cookie 登录");

        if (cookie == null || cookie.isEmpty()) {
            return new LoginResult(false, "Cookie 不能为空");
        }

        try {
            // 更新会话
            sessionManager.updateCookie(cookie);

            // 验证 Cookie
            if (!validateCookie(cookie)) {
                log.warn("Cookie 验证失败");
                return new LoginResult(false, "Cookie 已失效");
            }

            log.info("Cookie 登录成功");
            return new LoginResult(true, "Cookie 登录成功", null, cookie);

        } catch (Exception e) {
            log.error("Cookie 登录异常", e);
            return new LoginResult(false, "Cookie 登录异常: " + e.getMessage());
        }
    }

    @Override
    public boolean validateLogin() {
        String cookie = sessionManager.getCookie();
        return cookie != null && !cookie.isEmpty();
    }

    @Override
    public void logout() {
        sessionManager.close();
        log.info("登出成功");
    }

    /**
     * 验证 Cookie 是否有效
     *
     * 严格对照 Python 实现：
     * - 发送 POST 请求到课程列表接口
     * - 检查响应是否包含登录页面
     */
    private boolean validateCookie(String cookie) {
        try {
            // 构造请求
            FormBody.Builder formBuilder = new FormBody.Builder()
                    .add("courseType", "1")
                    .add("courseFolderId", "0")
                    .add("query", "")
                    .add("superstarClass", "0");

            Request request = new Request.Builder()
                    .url(COURSE_LIST_URL)
                    .post(formBuilder.build())
                    .addHeader("User-Agent", USER_AGENT)
                    .addHeader("Cookie", cookie)
                    .build();

            // 发送请求
            Response response = httpClient.newCall(request).execute();

            if (!response.isSuccessful()) {
                log.debug("Cookie 验证请求失败: status={}", response.code());
                return false;
            }

            // 解析响应
            String responseBody = response.body().string();

            // 检查是否包含登录页面
            if (responseBody.contains("passport2.chaoxing.com") || responseBody.toLowerCase().contains("login")) {
                log.debug("Cookie 验证失败: 响应包含登录页面");
                return false;
            }

            log.debug("Cookie 验证成功");
            return true;

        } catch (Exception e) {
            log.debug("Cookie 验证异常", e);
            return false;
        }
    }

    /**
     * 提取 Cookie
     */
    private String extractCookie(Response response) {
        StringBuilder cookieBuilder = new StringBuilder();

        Headers headers = response.headers();
        for (String name : headers.names()) {
            if (name.equalsIgnoreCase("Set-Cookie")) {
                for (String value : headers.values(name)) {
                    // 提取 Cookie 名称和值
                    String[] parts = value.split(";");
                    if (parts.length > 0) {
                        String[] keyValue = parts[0].split("=", 2);
                        if (keyValue.length == 2) {
                            if (cookieBuilder.length() > 0) {
                                cookieBuilder.append("; ");
                            }
                            cookieBuilder.append(keyValue[0]).append("=").append(keyValue[1]);
                        }
                    }
                }
            }
        }

        return cookieBuilder.toString();
    }
}
