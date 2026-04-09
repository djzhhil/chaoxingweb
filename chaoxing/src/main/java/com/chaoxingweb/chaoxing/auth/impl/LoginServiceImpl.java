package com.chaoxingweb.chaoxing.auth.impl;

import com.chaoxingweb.auth.service.LoginService;
import com.chaoxingweb.chaoxing.client.ChaoxingApiClient;
import com.chaoxingweb.chaoxing.core.CipherManager;
import com.chaoxingweb.chaoxing.core.SessionManager;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
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
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36";

    private final SessionManager sessionManager;
    private final CipherManager cipherManager;
    private final ChaoxingApiClient chaoxingApiClient;

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
            
            // 从 Cookie 中提取并设置 fid 和 uid
            extractAndSetFidUid(cookie, responseBody);

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
            
            // 从 Cookie 中提取并设置 fid 和 uid
            extractAndSetFidUid(cookie, null);

            // 验证 Cookie
            if (!chaoxingApiClient.validateCookie(cookie)) {
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

    /**
     * 从 Cookie 和响应中提取并设置 fid 和 uid
     *
     * @param cookie Cookie 字符串
     * @param responseBody 响应体（可选）
     */
    private void extractAndSetFidUid(String cookie, String responseBody) {
        try {
            log.debug("完整 Cookie 内容: {}", cookie);
            
            // 从 Cookie 中提取 UID (可能是 _uid 或 UID)
            String uid = extractCookieValue(cookie, "_uid");
            if (uid == null || uid.isEmpty()) {
                uid = extractCookieValue(cookie, "UID");
            }
            
            if (uid != null && !uid.isEmpty()) {
                sessionManager.setUid(uid);
                log.info("从 Cookie 中提取到 UID: {}", uid);
            } else {
                log.warn("未能从 Cookie 中提取 UID");
            }

            // 从 Cookie 中提取 FID (注意：是 "fid" 不是 "_fid")
            String fid = extractCookieValue(cookie, "fid");
            if (fid != null && !fid.isEmpty()) {
                sessionManager.setFid(fid);
                log.info("从 Cookie 中提取到 FID: {}", fid);
            } else {
                log.warn("未能从 Cookie 中提取 FID");
                
                // 如果提供了响应体，尝试从响应中获取
                if (responseBody != null && !responseBody.isEmpty()) {
                    fid = extractFidFromResponse(responseBody);
                    if (fid != null && !fid.isEmpty()) {
                        sessionManager.setFid(fid);
                        log.info("从响应中提取到 FID: {}", fid);
                    } else {
                        log.error("未能从响应中提取 FID，这可能导致视频学习失败");
                    }
                } else {
                    log.error("未提供响应体，无法提取 FID，这可能导致视频学习失败");
                }
            }
            
            log.debug("最终设置的 fid: {}, uid: {}", sessionManager.getFid(), sessionManager.getUid());

        } catch (Exception e) {
            log.error("提取 fid/uid 异常", e);
        }
    }

    /**
     * 从 Cookie 字符串中提取指定名称的值
     *
     * @param cookie Cookie 字符串
     * @param name Cookie 名称
     * @return Cookie 值
     */
    private String extractCookieValue(String cookie, String name) {
        if (cookie == null || cookie.isEmpty() || name == null || name.isEmpty()) {
            return null;
        }

        // 查找 name=value 模式
        String pattern = name + "=([^;]+)";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(cookie);

        if (m.find()) {
            return m.group(1);
        }

        return null;
    }

    /**
     * 从响应体中提取 FID
     *
     * @param responseBody 响应体
     * @return FID
     */
    private String extractFidFromResponse(String responseBody) {
        if (responseBody == null || responseBody.isEmpty()) {
            return null;
        }

        try {
            // 尝试从 JSON 响应中提取 fid
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(responseBody);

            // 尝试多个可能的字段名
            if (jsonNode.has("fid")) {
                return jsonNode.get("fid").asText();
            }
            if (jsonNode.has("_fid")) {
                return jsonNode.get("_fid").asText();
            }
            if (jsonNode.has("data") && jsonNode.get("data").has("fid")) {
                return jsonNode.get("data").get("fid").asText();
            }

        } catch (Exception e) {
            log.debug("从响应中解析 fid 失败: {}", e.getMessage());
        }

        return null;
    }
}
