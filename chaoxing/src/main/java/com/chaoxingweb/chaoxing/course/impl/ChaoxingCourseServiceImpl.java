package com.chaoxingweb.chaoxing.course.impl;

import com.chaoxingweb.chaoxing.adapter.CourseAdapter;
import com.chaoxingweb.chaoxing.client.ChaoxingApiClient;
import com.chaoxingweb.chaoxing.converter.CourseConverter;
import com.chaoxingweb.chaoxing.core.SessionManager;
import com.chaoxingweb.chaoxing.dto.CourseDTO;
import com.chaoxingweb.chaoxing.course.ChaoxingChapterService;
import com.chaoxingweb.chaoxing.course.ChaoxingCourseService;
import com.chaoxingweb.chaoxing.vo.CourseVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 超星课程服务实现
 *
 * @author 小克 🐕💎
 * @since 2026-03-31
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChaoxingCourseServiceImpl implements ChaoxingCourseService {

    private final ChaoxingApiClient chaoxingApiClient;
    private final CourseAdapter courseAdapter;
    private final CourseConverter courseConverter;
    private final SessionManager sessionManager;
    private final ChaoxingChapterService chapterService;

    @Override
    public List<CourseDTO> getCourseList() {
        try {
            log.info("开始获取课程列表...");

            // 调用 API 客户端获取 HTML
            String html = chaoxingApiClient.getCourseListHtml();

            if (html == null || html.isEmpty()) {
                log.error("获取课程列表失败：HTML 为空");
                return new ArrayList<>();
            }

            // 使用适配器解析 HTML
            List<CourseDTO> courseList = courseAdapter.parseCourseList(html);

            log.info("课程列表获取成功，共{}门课程", courseList.size());
            return courseList;

        } catch (Exception e) {
            log.error("获取课程列表异常", e);
            return new ArrayList<>();
        }
    }

    @Override
    public CourseDTO getCourseDetail(String courseId) {
        // TODO: 实现获取课程详情逻辑
        log.info("获取课程详情功能待实现: courseId={}", courseId);
        return new CourseDTO();
    }

    @Override
    public List<CourseDTO> getCoursePoint(String courseId, String clazzId, String cpi) {
        try {
            log.info("开始获取课程章节: courseId={}, clazzId={}, cpi={}", courseId, clazzId, cpi);

            // 调用章节服务获取章节列表
            return chapterService.getChapterList(courseId, clazzId, cpi)
                    .stream()
                    .map(chapter -> {
                        // 将 ChapterDTO 转换为 CourseDTO（临时方案，保持接口兼容）
                        CourseDTO courseDTO = new CourseDTO();
                        courseDTO.setCourseId(chapter.getCourseId());
                        courseDTO.setClazzId(chapter.getClazzId());
                        courseDTO.setCpi(chapter.getCpi());
                        courseDTO.setCourseName(chapter.getTitle());
                        return courseDTO;
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("获取课程章节异常", e);
            return new ArrayList<>();
        }
    }
}
