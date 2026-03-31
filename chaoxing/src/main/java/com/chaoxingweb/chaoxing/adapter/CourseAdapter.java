package com.chaoxingweb.chaoxing.adapter;

import com.chaoxingweb.chaoxing.dto.CourseDTO;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 课程适配器
 *
 * 职责：
 * - 将超星 API 响应转换为内部模型
 * - 解析 HTML 响应
 * - 不包含业务逻辑
 *
 * @author 小克 🐕💎
 * @since 2026-03-31
 */
@Slf4j
@Component
public class CourseAdapter {

    /**
     * 解析课程列表 HTML
     *
     * @param html HTML内容
     * @return 课程列表
     */
    public List<CourseDTO> parseCourseList(String html) {
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
            log.error("解析课程列表HTML异常", e);
        }

        return courseList;
    }
}
