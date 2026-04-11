package com.chaoxingweb.chaoxing.client;

import com.chaoxingweb.chaoxing.core.SessionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

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
    private static final String COURSE_POINT_URL = "https://mooc2-ans.chaoxing.com/mooc2-ans/mycourse/studentcourse";
    private static final String JOB_CARDS_URL = "https://mooc1.chaoxing.com/mooc-ans/knowledge/cards";
    private static final String VIDEO_LOG_URL = "https://mooc1.chaoxing.com/mooc-ans/multimedia/log/a/";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36";

    private final SessionManager sessionManager;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

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

    /**
     * 获取课程章节列表 HTML
     *
     * @param courseId 课程ID
     * @param clazzId 班级ID
     * @param cpi CPI
     * @return 章节列表 HTML
     */
    public String getCoursePointHtml(String courseId, String clazzId, String cpi) {
        try {
            log.info("开始获取课程章节列表: courseId={}, clazzId={}, cpi={}", courseId, clazzId, cpi);

            // 获取会话
            String cookies = sessionManager.getCookie();
            if (cookies == null || cookies.isEmpty()) {
                log.error("未登录，无法获取章节列表");
                return "";
            }

            // 构造URL
            String url = String.format("%s?courseid=%s&clazzid=%s&cpi=%s&ut=s", 
                    COURSE_POINT_URL, courseId, clazzId, cpi);

            // 构造请求头
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", USER_AGENT);
            headers.set("Cookie", cookies);

            // 发送GET请求
            HttpEntity<Void> request = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

            // 检查响应状态
            if (response.getStatusCode() != HttpStatus.OK) {
                log.error("获取章节列表失败，状态码：{}", response.getStatusCode());
                return "";
            }

            log.info("章节列表获取成功");
            return response.getBody();

        } catch (Exception e) {
            log.error("获取章节列表异常", e);
            return "";
        }
    }

    /**
     * 获取章节任务卡片 HTML
     *
     * @param clazzId 班级ID
     * @param courseId 课程ID
     * @param knowledgeId 知识点ID（章节ID）
     * @param cpi CPI
     * @param num 任务数量（0-6）
     * @return 任务卡片 HTML
     */
    public String getJobCardsHtml(String clazzId, String courseId, String knowledgeId, String cpi, String num) {
        try {
            log.trace("开始获取任务卡片: courseId={}, clazzId={}, knowledgeId={}, num={}", 
                    courseId, clazzId, knowledgeId, num);

            // 获取会话
            String cookies = sessionManager.getCookie();
            if (cookies == null || cookies.isEmpty()) {
                log.error("未登录，无法获取任务卡片");
                return "";
            }

            // 构造URL参数
            String url = String.format("%s?clazzid=%s&courseid=%s&knowledgeid=%s&ut=s&cpi=%s&v=2025-0424-1038-3&mooc2=1&num=%s",
                    JOB_CARDS_URL, clazzId, courseId, knowledgeId, cpi, num);

            // 构造请求头
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", USER_AGENT);
            headers.set("Cookie", cookies);

            // 发送GET请求
            HttpEntity<Void> request = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

            // 检查响应状态
            if (response.getStatusCode() != HttpStatus.OK) {
                log.error("获取任务卡片失败，状态码：{}", response.getStatusCode());
                return "";
            }

            log.trace("任务卡片获取成功");
            return response.getBody();

        } catch (Exception e) {
            log.error("获取任务卡片异常", e);
            return "";
        }
    }

    /**
     * 上报视频学习进度
     *
     * @param cpi CPI
     * @param dtoken 文档token
     * @param params 请求参数
     * @return 响应JSON字符串
     */
    public String reportVideoProgress(String cpi, String dtoken, Map<String, String> params) {
        try {
            log.trace("上报视频进度: cpi={}, dtoken={}", cpi, dtoken);

            // 获取会话
            String cookies = sessionManager.getCookie();
            if (cookies == null || cookies.isEmpty()) {
                log.error("未登录，无法上报进度");
                return null;
            }

            // 构造URL
            String url = String.format("%s%s/%s", VIDEO_LOG_URL, cpi, dtoken);

            // 构造请求参数
            MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                data.add(entry.getKey(), entry.getValue());
            }

            // 构造请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("User-Agent", USER_AGENT);
            headers.set("Cookie", cookies);
            headers.set("Referer", "https://mooc1.chaoxing.com/");

            // 发送GET请求（注意：超星使用GET请求上报进度）
            HttpEntity<Void> request = new HttpEntity<>(headers);
            
            // 构建带参数的URL
            StringBuilder urlBuilder = new StringBuilder(url);
            boolean first = true;
            for (Map.Entry<String, String> entry : params.entrySet()) {
                urlBuilder.append(first ? "?" : "&");
                urlBuilder.append(entry.getKey()).append("=").append(entry.getValue());
                first = false;
            }
            
            ResponseEntity<String> response = restTemplate.exchange(
                    urlBuilder.toString(), HttpMethod.GET, request, String.class);

            // 检查响应状态
            if (response.getStatusCode() != HttpStatus.OK) {
                log.error("上报视频进度失败，状态码：{}", response.getStatusCode());
                return null;
            }

            String responseBody = response.getBody();
            log.trace("视频进度上报成功: {}", responseBody);
            return responseBody;

        } catch (Exception e) {
            log.error("上报视频进度异常", e);
            return null;
        }
    }

    /**
     * 获取视频/音频状态信息
     *
     * @param objectId 对象ID
     * @param fid FID
     * @param isVideo 是否为视频（true=视频，false=音频）
     * @return 响应JSON字符串
     */
    public String getMediaStatus(String objectId, String fid, boolean isVideo) {
        try {
            log.info("开始获取媒体状态: objectId={}, fid={}, isVideo={}", objectId, fid, isVideo);

            // 参数验证
            if (objectId == null || objectId.isEmpty()) {
                log.error("objectId 为空，无法获取媒体状态");
                return null;
            }
            
            if (fid == null || fid.isEmpty()) {
                log.error("fid 为空，无法获取媒体状态");
                return null;
            }

            // 获取会话
            String cookies = sessionManager.getCookie();
            if (cookies == null || cookies.isEmpty()) {
                log.error("未登录，无法获取媒体状态");
                return null;
            }
            
            log.debug("Cookie 长度: {}", cookies.length());

            // 构造URL（对照Python实现）
            // Python: f"https://mooc1.chaoxing.com/ananas/status/{job['objectid']}?k={self.get_fid()}&flag=normal"
            String url = String.format("https://mooc1.chaoxing.com/ananas/status/%s?k=%s&flag=normal",
                    objectId, fid);
            log.info("请求 URL: {}", url);

            // 构造请求头
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", USER_AGENT);
            headers.set("Cookie", cookies);
            if (isVideo) {
                headers.set("Referer", "https://mooc1.chaoxing.com/ananas/modules/video/index.html");
            } else {
                headers.set("Referer", "https://mooc1.chaoxing.com/ananas/modules/audio/index.html");
            }
            
            log.debug("请求头 - User-Agent: {}", USER_AGENT);
            log.debug("请求头 - Referer: {}", headers.getFirst("Referer"));

            // 发送GET请求
            HttpEntity<Void> request = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

            // 检查响应状态
            if (response.getStatusCode() != HttpStatus.OK) {
                log.error("获取媒体状态失败，状态码：{}", response.getStatusCode());
                log.error("响应内容: {}", response.getBody());
                return null;
            }

            String responseBody = response.getBody();
            if (responseBody == null || responseBody.isEmpty()) {
                log.error("响应体为空");
                return null;
            }
            
            log.info("媒体状态获取成功，响应长度: {}", responseBody.length());
            log.debug("响应内容: {}", responseBody);
            return responseBody;

        } catch (Exception e) {
            log.error("获取媒体状态异常: objectId={}, fid={}, isVideo={}", objectId, fid, isVideo, e);
            log.error("异常类型: {}", e.getClass().getName());
            log.error("异常消息: {}", e.getMessage());
            if (e.getCause() != null) {
                log.error("异常原因: {}", e.getCause().getMessage());
            }
            return null;
        }
    }

    /**
     * 完成文档学习任务
     *
     * @param jobId 任务ID
     * @param knowledgeId 知识点ID
     * @param courseId 课程ID
     * @param clazzId 班级ID
     * @param jtoken 任务token
     * @return 是否成功
     */
    public boolean completeDocumentStudy(String jobId, String knowledgeId, String courseId, 
                                         String clazzId, String jtoken) {
        try {
            log.trace("完成文档学习: jobId={}", jobId);

            // 获取会话
            String cookies = sessionManager.getCookie();
            if (cookies == null || cookies.isEmpty()) {
                log.error("未登录，无法完成文档学习");
                return false;
            }

            // 构造URL
            long timestamp = System.currentTimeMillis();
            String url = String.format(
                    "https://mooc1.chaoxing.com/ananas/job/document?jobid=%s&knowledgeid=%s&courseid=%s&clazzid=%s&jtoken=%s&_dc=%d",
                    jobId, knowledgeId, courseId, clazzId, jtoken, timestamp);

            // 构造请求头
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", USER_AGENT);
            headers.set("Cookie", cookies);
            headers.set("Referer", "https://mooc1.chaoxing.com/");

            // 发送GET请求
            HttpEntity<Void> request = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

            // 检查响应状态
            if (response.getStatusCode() != HttpStatus.OK) {
                log.error("完成文档学习失败，状态码：{}", response.getStatusCode());
                return false;
            }

            log.trace("文档学习完成");
            return true;

        } catch (Exception e) {
            log.error("完成文档学习异常", e);
            return false;
        }
    }

    /**
     * 完成阅读学习任务
     *
     * @param jobId 任务ID
     * @param knowledgeId 知识点ID
     * @param courseId 课程ID
     * @param clazzId 班级ID
     * @param jtoken 任务token
     * @return 是否成功
     */
    public boolean completeReadStudy(String jobId, String knowledgeId, String courseId,
                                     String clazzId, String jtoken) {
        try {
            log.info("完成阅读学习: jobId={}, knowledgeId={}", jobId, knowledgeId);

            // 获取会话
            String cookies = sessionManager.getCookie();
            if (cookies == null || cookies.isEmpty()) {
                log.error("未登录，无法完成阅读学习");
                return false;
            }

            // 构造URL - 参考Python实现: https://mooc1.chaoxing.com/ananas/job/readv2
            String url = String.format(
                    "https://mooc1.chaoxing.com/ananas/job/readv2?jobid=%s&knowledgeid=%s&courseid=%s&clazzid=%s&jtoken=%s",
                    jobId, knowledgeId, courseId, clazzId, jtoken);

            // 构造请求头
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", USER_AGENT);
            headers.set("Cookie", cookies);
            headers.set("Referer", "https://mooc1.chaoxing.com/");

            // 发送GET请求
            HttpEntity<Void> request = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

            // 检查响应状态
            if (response.getStatusCode() != HttpStatus.OK) {
                log.error("完成阅读学习失败，状态码：{}，响应：{}", response.getStatusCode(), response.getBody());
                return false;
            }

            // 解析响应JSON
            String responseBody = response.getBody();
            if (responseBody != null && !responseBody.isEmpty()) {
                try {
                    com.fasterxml.jackson.databind.JsonNode jsonNode = objectMapper.readTree(responseBody);
                    String msg = jsonNode.has("msg") ? jsonNode.get("msg").asText() : "未知";
                    log.info("阅读任务学习完成: {}", msg);
                } catch (Exception e) {
                    log.debug("解析阅读任务响应失败: {}", e.getMessage());
                }
            }

            log.trace("阅读学习完成");
            return true;

        } catch (Exception e) {
            log.error("完成阅读学习异常: jobId={}", jobId, e);
            return false;
        }
    }

    /**
     * 获取测验题目页面HTML
     *
     * @param workId 工作ID（不含work-前缀）
     * @param jobid 作业ID（含work-前缀）
     * @param knowledgeId 知识点ID
     * @param ktoken KToken
     * @param cpi CPI
     * @param clazzId 班级ID
     * @param courseId 课程ID
     * @param enc 加密参数
     * @return 测验页面HTML内容
     */
    public String getWorkQuestionsHtml(String workId, String jobid, String knowledgeId,
                                       String ktoken, String cpi, String clazzId,
                                       String courseId, String enc) {
        try {
            log.info("开始获取测验题目: workId={}, jobid={}", workId, jobid);

            // 获取会话
            String cookies = sessionManager.getCookie();
            if (cookies == null || cookies.isEmpty()) {
                log.error("未登录，无法获取测验题目");
                return "";
            }

            // 构造URL - 参考Python实现
            String url = "https://mooc1.chaoxing.com/mooc-ans/api/work";

            // 构造请求参数
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("api", "1");
            params.add("workId", workId);
            params.add("jobid", jobid);
            params.add("originJobId", jobid);
            params.add("needRedirect", "true");
            params.add("skipHeader", "true");
            params.add("knowledgeid", knowledgeId);
            params.add("ktoken", ktoken);
            params.add("cpi", cpi);
            params.add("ut", "s");
            params.add("clazzId", clazzId);
            params.add("type", "");
            params.add("enc", enc);
            params.add("mooc2", "1");
            params.add("courseid", courseId);

            // 构造请求头
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", USER_AGENT);
            headers.set("Cookie", cookies);
            headers.set("Referer", "https://mooc1.chaoxing.com/");

            // 发送GET请求
            HttpEntity<Void> request = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    url + "?" + buildQueryString(params),
                    HttpMethod.GET,
                    request,
                    String.class
            );

            // 检查响应状态
            if (response.getStatusCode() != HttpStatus.OK) {
                log.error("获取测验题目失败，状态码：{}", response.getStatusCode());
                return "";
            }

            String html = response.getBody();
            if (html == null || html.isEmpty()) {
                log.error("获取测验题目失败，响应为空");
                return "";
            }

            log.info("测验题目获取成功，HTML长度: {}", html.length());
            return html;

        } catch (Exception e) {
            log.error("获取测验题目异常: workId={}", workId, e);
            return "";
        }
    }

    /**
     * 提交测验答案
     *
     * @param formData 表单数据（包含所有答案字段）
     * @return 是否提交成功
     */
    public boolean submitWorkAnswers(Map<String, String> formData) {
        try {
            log.info("开始提交测验答案...");

            // 获取会话
            String cookies = sessionManager.getCookie();
            if (cookies == null || cookies.isEmpty()) {
                log.error("未登录，无法提交测验答案");
                return false;
            }

            // 构造URL
            String url = "https://mooc1.chaoxing.com/mooc-ans/work/addStudentWorkNew";

            // 构造请求体
            MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
            formData.forEach(requestBody::add);

            // 构造请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("User-Agent", USER_AGENT);
            headers.set("Cookie", cookies);
            headers.set("Referer", "https://mooc1.chaoxing.com/");
            headers.set("X-Requested-With", "XMLHttpRequest");
            headers.set("Accept", "application/json, text/javascript, */*; q=0.01");

            // 发送POST请求
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            // 检查响应状态
            if (response.getStatusCode() != HttpStatus.OK) {
                log.error("提交测验答案失败，状态码：{}", response.getStatusCode());
                return false;
            }

            // 解析响应JSON
            String responseBody = response.getBody();
            if (responseBody != null && !responseBody.isEmpty()) {
                try {
                    com.fasterxml.jackson.databind.JsonNode jsonNode = objectMapper.readTree(responseBody);
                    boolean status = jsonNode.has("status") && jsonNode.get("status").asBoolean();
                    String msg = jsonNode.has("msg") ? jsonNode.get("msg").asText() : "未知";

                    if (status) {
                        log.info("✅ 提交测验答案成功: {}", msg);
                        return true;
                    } else {
                        log.error("❌ 提交测验答案失败: {}", msg);
                        return false;
                    }
                } catch (Exception e) {
                    log.error("解析提交响应失败: {}", responseBody, e);
                    return false;
                }
            }

            log.error("提交测验答案失败，响应为空");
            return false;

        } catch (Exception e) {
            log.error("提交测验答案异常", e);
            return false;
        }
    }

    /**
     * 构建查询字符串
     *
     * @param params 参数Map
     * @return 查询字符串
     */
    private String buildQueryString(MultiValueMap<String, String> params) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, List<String>> entry : params.entrySet()) {
            for (String value : entry.getValue()) {
                if (!first) {
                    sb.append("&");
                }
                sb.append(entry.getKey()).append("=").append(value);
                first = false;
            }
        }
        return sb.toString();
    }

    /**
     * 获取直播状态信息
     *
     * @param url 直播状态查询URL
     * @return 直播状态信息（JSON对象或null）
     */
    public Object getLiveStatus(String url) {
        try {
            log.trace("获取直播状态: {}", url);

            // 获取会话
            String cookies = sessionManager.getCookie();
            if (cookies == null || cookies.isEmpty()) {
                log.error("未登录，无法获取直播状态");
                return null;
            }

            // 构造请求头
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", USER_AGENT);
            headers.set("Cookie", cookies);
            headers.set("X-Requested-With", "XMLHttpRequest");

            // 发送GET请求
            HttpEntity<Void> request = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                log.warn("获取直播状态失败，状态码：{}", response.getStatusCode());
                return null;
            }

            // 解析JSON响应
            String responseBody = response.getBody();
            if (responseBody != null && !responseBody.isEmpty()) {
                return objectMapper.readTree(responseBody);
            }

            return null;

        } catch (Exception e) {
            log.error("获取直播状态异常", e);
            return null;
        }
    }

    /**
     * 提交直播观看时长
     *
     * @param url 时长提交URL
     * @return 是否成功
     */
    public boolean submitLiveDuration(String url) {
        try {
            log.trace("提交直播时长: {}", url);

            // 获取会话
            String cookies = sessionManager.getCookie();
            if (cookies == null || cookies.isEmpty()) {
                log.error("未登录，无法提交直播时长");
                return false;
            }

            // 构造请求头
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", USER_AGENT);
            headers.set("Cookie", cookies);

            // 发送GET请求
            HttpEntity<Void> request = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                log.warn("提交直播时长失败，状态码：{}", response.getStatusCode());
                return false;
            }

            // 检查响应内容（成功时返回"@success"）
            String responseBody = response.getBody();
            boolean success = "@success".equals(responseBody != null ? responseBody.trim() : "");
            
            if (success) {
                log.debug("直播时长提交成功");
            } else {
                log.warn("直播时长提交响应异常: {}", responseBody);
            }

            return success;

        } catch (Exception e) {
            log.error("提交直播时长异常", e);
            return false;
        }
    }

    /**
     * 学习空页面（访问章节学习页面以标记完成）
     *
     * @param courseId 课程ID
     * @param clazzId 班级ID
     * @param chapterId 章节ID
     * @param cpi CPI
     * @return 是否成功
     */
    public boolean completeEmptyPage(String courseId, String clazzId, String chapterId, String cpi) {
        try {
            log.debug("开始学习空页面: courseId={}, chapterId={}", courseId, chapterId);

            // 获取会话
            String cookies = sessionManager.getCookie();
            if (cookies == null || cookies.isEmpty()) {
                log.error("未登录，无法学习空页面");
                return false;
            }

            // 构造URL参数（与 Python 实现保持一致）
            String url = "https://mooc1.chaoxing.com/mooc-ans/mycourse/studentstudyAjax";
            
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("courseId", courseId);
            params.add("clazzid", clazzId);
            params.add("chapterId", chapterId);
            params.add("cpi", cpi);
            params.add("verificationcode", "");
            params.add("mooc2", "1");
            params.add("microTopicId", "0");
            params.add("editorPreview", "0");

            // 构造请求头
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", USER_AGENT);
            headers.set("Cookie", cookies);

            // 发送GET请求
            HttpEntity<Void> request = new HttpEntity<>(headers);
            
            // 构建完整URL
            StringBuilder urlBuilder = new StringBuilder(url);
            urlBuilder.append("?");
            boolean first = true;
            for (Map.Entry<String, List<String>> entry : params.entrySet()) {
                for (String value : entry.getValue()) {
                    if (!first) {
                        urlBuilder.append("&");
                    }
                    urlBuilder.append(entry.getKey()).append("=").append(value);
                    first = false;
                }
            }
            
            ResponseEntity<String> response = restTemplate.exchange(
                    urlBuilder.toString(), 
                    HttpMethod.GET, 
                    request, 
                    String.class
            );

            if (response.getStatusCode() != HttpStatus.OK) {
                log.warn("空页面学习失败，状态码：{}", response.getStatusCode());
                return false;
            }

            log.debug("空页面学习成功");
            return true;

        } catch (Exception e) {
            log.error("空页面学习异常", e);
            return false;
        }
    }
}
