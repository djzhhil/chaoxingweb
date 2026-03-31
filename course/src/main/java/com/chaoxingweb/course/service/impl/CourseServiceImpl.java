package com.chaoxingweb.course.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.chaoxingweb.chaoxing.dto.CourseDTO;
import com.chaoxingweb.chaoxing.facade.ChaoxingFacade;
import com.chaoxingweb.chaoxing.vo.CourseVO;
import com.chaoxingweb.course.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 课程服务实现
 *
 * @author 小克 🐕💎
 * @since 2026-03-31
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final ChaoxingFacade chaoxingFacade;

    @Override
    public List<com.chaoxingweb.course.vo.CourseVO> getCourseList() {
        log.info("开始获取课程列表");

        try {
            // 调用 ChaoxingFacade 获取课程列表
            List<CourseVO> chaoxingCourses = chaoxingFacade.getCourseList();

            // 转换为业务 VO
            List<com.chaoxingweb.course.vo.CourseVO> courses = chaoxingCourses.stream()
                    .map(this::convertToCourseVO)
                    .collect(Collectors.toList());

            log.info("课程列表获取成功，共{}门课程", courses.size());
            return courses;

        } catch (Exception e) {
            log.error("获取课程列表失败", e);
            throw new RuntimeException("获取课程列表失败: " + e.getMessage());
        }
    }

    @Override
    public com.chaoxingweb.course.vo.CourseVO getCourseDetail(String courseId) {
        log.info("开始获取课程详情: courseId={}", courseId);

        try {
            // 调用 ChaoxingFacade 获取课程详情
            CourseVO chaoxingCourse = chaoxingFacade.getCourseDetail(courseId);

            // 转换为业务 VO
            com.chaoxingweb.course.vo.CourseVO course = convertToCourseVO(chaoxingCourse);

            log.info("课程详情获取成功: courseId={}", courseId);
            return course;

        } catch (Exception e) {
            log.error("获取课程详情失败: courseId={}", courseId, e);
            throw new RuntimeException("获取课程详情失败: " + e.getMessage());
        }
    }

    @Override
    public int syncCourseList() {
        log.info("开始同步课程列表");

        try {
            // 调用 ChaoxingFacade 获取课程列表
            List<CourseVO> chaoxingCourses = chaoxingFacade.getCourseList();

            // 这里应该将课程列表保存到数据库
            // TODO: 实现数据库保存逻辑

            log.info("课程列表同步成功，共{}门课程", chaoxingCourses.size());
            return chaoxingCourses.size();

        } catch (Exception e) {
            log.error("同步课程列表失败", e);
            throw new RuntimeException("同步课程列表失败: " + e.getMessage());
        }
    }

    /**
     * 转换为业务 VO
     */
    private com.chaoxingweb.course.vo.CourseVO convertToCourseVO(CourseVO chaoxingCourse) {
        return com.chaoxingweb.course.vo.CourseVO.builder()
                .courseId(chaoxingCourse.getCourseId())
                .clazzId(chaoxingCourse.getClazzId())
                .cpi(chaoxingCourse.getCpi())
                .courseName(chaoxingCourse.getCourseName())
                .teacherName(chaoxingCourse.getTeacherName())
                .schoolName(chaoxingCourse.getSchoolName())
                .description(chaoxingCourse.getDescription())
                .coverUrl(chaoxingCourse.getCoverUrl())
                .status(chaoxingCourse.getStatus())
                .synced(true)
                .syncTime(System.currentTimeMillis())
                .build();
    }
}
