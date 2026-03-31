package com.chaoxingweb.chaoxing.course.impl;

import com.chaoxingweb.chaoxing.core.SessionManager;
import com.chaoxingweb.chaoxing.dto.CourseDTO;
import com.chaoxingweb.chaoxing.course.CourseService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 课程服务实现
 *
 * @author 小克 🐕💎
 * @since 2026-03-31
 */
@Service
public class CourseServiceImpl implements CourseService {

    private static final Logger logger = LoggerFactory.getLogger(CourseServiceImpl.class);

    private final SessionManager sessionManager;
    private final RestTemplate restTemplate;

    @Autowired
    public CourseServiceImpl(SessionManager sessionManager, RestTemplate restTemplate) {
        this.sessionManager = sessionManager;
        this.restTemplate = restTemplate;
    }

    @Override
    public List<CourseDTO> getCourseList() {
        try {
            logger.info("开始获取课程列表...");

            // 获取会话
            String cookies = sessionManager.getCookie();
            if (cookies == null || cookies.isEmpty()) {
                logger.error("未登录，无法获取课程列表");
                return new ArrayList<>();
            }

            // 构造请求URL
            String url = "https://mooc2-ans.chaoxing.com/mooc2-ans/visit/courselistdata";

            // 构造请求参数
            MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
            data.add("courseType", "1");
            data.add("courseFolderId", "0");
            data.add("query", "");
            data.add("superstarClass", "0");

            // 构造请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36");
            headers.set("Referer", "https://mooc2-ans.chaoxing.com/mooc2-ans/visit/interaction?moocDomain=https://mooc1-1.chaoxing.com/mooc-ans");
            headers.set("Cookie", cookies);

            // 发送POST请求
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(data, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            // 检查响应状态
            if (response.getStatusCode() != HttpStatus.OK) {
                logger.error("获取课程列表失败，状态码：{}", response.getStatusCode());
                return new ArrayList<>();
            }

            // 解析HTML响应
            String html = response.getBody();
            List<CourseDTO> courseList = decodeCourseList(html);

            logger.info("课程列表获取成功，共{}门课程", courseList.size());
            return courseList;

        } catch (Exception e) {
            logger.error("获取课程列表异常", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<CourseDTO> getCoursePoint(String courseId, String clazzId, String cpi) {
        // TODO: 实现获取课程章节逻辑
        logger.info("获取课程章节功能待实现");
        return new ArrayList<>();
    }

    /**
     * 解析课程列表HTML
     *
     * @param html HTML内容
     * @return 课程列表
     */
    private List<CourseDTO> decodeCourseList(String html) {
        List<CourseDTO> courseList = new ArrayList<>();

        try {
            Document doc = Jsoup.parse(html);
            Elements courses = doc.select("div.course");

            for (Element course : courses) {
                // 跳过未开放课程
                if (course.selectFirst("a.not-open-tip") != null || course.selectFirst("div.not-open-tip") != null) {
                    continue;
                }

                // 提取课程信息
                String id = course.attr("id");
                String info = course.attr("info");
                String roleid = course.attr("roleid");

                // 提取clazzId
                Element clazzIdElement = course.selectFirst("input.clazzId");
                String clazzId = clazzIdElement != null ? clazzIdElement.attr("value") : "";

                // 提取courseId
                Element courseIdElement = course.selectFirst("input.courseId");
                String courseId = courseIdElement != null ? courseIdElement.attr("value") : "";

                // 提取cpi
                Element linkElement = course.selectFirst("a");
                String cpi = "";
                if (linkElement != null) {
                    String href = linkElement.attr("href");
                    Pattern pattern = Pattern.compile("cpi=(.*?)&");
                    Matcher matcher = pattern.matcher(href);
                    if (matcher.find()) {
                        cpi = matcher.group(1);
                    }
                }

                // 提取课程名称
                Element courseNameElement = course.selectFirst("span.course-name");
                String courseName = courseNameElement != null ? courseNameElement.attr("title") : "";

                // 提取课程描述
                Element descElement = course.selectFirst("p.margint10");
                String desc = descElement != null ? descElement.attr("title") : "";

                // 提取教师名称
                Element teacherElement = course.selectFirst("p.color3");
                String teacherName = teacherElement != null ? teacherElement.attr("title") : "";

                // 创建CourseDTO
                CourseDTO courseDTO = new CourseDTO();
                courseDTO.setCourseId(courseId);
                courseDTO.setClazzId(clazzId);
                courseDTO.setCpi(cpi);
                courseDTO.setCourseName(courseName);
                courseDTO.setTeacherName(teacherName);
                courseDTO.setSchoolName(desc);

                courseList.add(courseDTO);
            }

        } catch (Exception e) {
            logger.error("解析课程列表HTML异常", e);
        }

        return courseList;
    }
}
