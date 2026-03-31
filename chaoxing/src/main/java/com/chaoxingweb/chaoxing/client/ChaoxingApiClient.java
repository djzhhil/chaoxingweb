package com.chaoxingweb.chaoxing.client;

import com.chaoxingweb.chaoxing.core.SessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * 超星 API 客户端
 *
 * 职责：
 * - 与超星 API 通信
 * - 处理 HTTP 请求和响应
 * - 不包含业务逻辑
 *
 * @author 小克 🐕💎
 * @since 2026-03-31
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChaoxingApiClient {

    private static final String COURSE_LIST_URL = "https://mooc2-ans.chaoxing.com/mooc2-ans/visit/courselistdata";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36";

    private final SessionManager sessionManager;
    private final RestTemplate restTemplate;

    /**
     * 获取课程列表 HTML
     *
     * @return 课程列表 HTML
     */
    public String getCourseListHtml() {
        try {
            log.info("开始获取课程列表...");

            // 获取会话
            String cookies = sessionManager.getCookie();
            if (cookies == null || cookies.isEmpty()) {
                log.error("未登录，无法获取课程列表");
                return "";
            }

            // 构造请求参数
            MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
            data.add("courseType", "1");
            data.add("courseFolderId", "0");
            data.add("query", "");
            data.add("superstarClass", "0");

            // 构造请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("User-Agent", USER_AGENT);
            headers.set("Referer", "https://mooc2-ans.chaoxing.com/mooc2-ans/visit/interaction?moocDomain=https://mooc1-1.chaoxing.com/mooc-ans");
            headers.set("Cookie", cookies);

            // 发送POST请求
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(data, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(COURSE_LIST_URL, request, String.class);

            // 检查响应状态
            if (response.getStatusCode() != HttpStatus.OK) {
                log.error("获取课程列表失败，状态码：{}", response.getStatusCode());
                return "";
            }

            log.info("课程列表获取成功");
            return response.getBody();

        } catch (Exception e) {
            log.error("获取课程列表异常", e);
            return "";
        }
    }

    /**
     * 验证 Cookie 是否有效
     *
     * @param cookie Cookie
     * @return 是否有效
     */
    public boolean validateCookie(String cookie) {
        try {
            // 构造请求参数
            MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
            data.add("courseType", "1");
            data.add("courseFolderId", "0");
            data.add("query", "");
            data.add("superstarClass", "0");

            // 构造请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("User-Agent", USER_AGENT);
            headers.set("Cookie", cookie);

            // 发送POST请求
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(data, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(COURSE_LIST_URL, request, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.debug("Cookie 验证请求失败: status={}", response.getStatusCode());
                return false;
            }

            // 解析响应
            String responseBody = response.getBody();

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
}
