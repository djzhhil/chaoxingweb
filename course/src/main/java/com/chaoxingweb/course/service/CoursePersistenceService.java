package com.chaoxingweb.course.service;

import com.chaoxingweb.auth.entity.Course;
import com.chaoxingweb.auth.repository.CourseRepository;
import com.chaoxingweb.chaoxing.dto.CourseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 课程持久化服务
 * 
 * 职责：
 * - 管理课程的数据库持久化
 * - 提供缓存优先的查询策略
 * - 同步超星API数据到数据库
 *
 * @author 小克 🐕💎
 * @since 2026-04-10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CoursePersistenceService {

    private final CourseRepository courseRepository;

    /**
     * 课程同步间隔（小时）
     */
    private static final int SYNC_INTERVAL_HOURS = 24;

    /**
     * 获取用户的课程列表（优先从数据库读取）
     *
     * @return 课程列表
     */
    public List<Course> getUserCourses() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            log.warn("用户未登录，无法获取课程列表");
            return List.of();
        }

        // 从数据库查询
        List<Course> courses = courseRepository.findByUserIdOrderByUpdateTimeDesc(userId);
        
        if (courses.isEmpty()) {
            log.info("数据库中无课程记录，需要先调用 syncCoursesFromApi() 同步");
        } else {
            log.debug("从数据库获取{}门课程", courses.size());
        }
        
        return courses;
    }

    /**
     * 从API同步课程列表到数据库
     *
     * @param courseDTOs API返回的课程列表
     * @return 同步后的课程列表
     */
    @Transactional
    public List<Course> syncCoursesFromApi(List<CourseDTO> courseDTOs) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            log.warn("用户未登录，无法同步课程");
            return List.of();
        }

        log.info("开始同步课程列表，共{}门课程", courseDTOs.size());

        LocalDateTime now = LocalDateTime.now();
        List<Course> savedCourses = courseDTOs.stream()
                .map(dto -> {
                    // 查询是否已存在
                    Course existingCourse = courseRepository
                            .findByUserIdAndCourseId(userId, dto.getCourseId())
                            .orElse(null);

                    Course course;
                    if (existingCourse != null) {
                        // 更新现有记录
                        course = existingCourse;
                    } else {
                        // 创建新记录
                        course = new Course();
                        course.setUserId(userId);
                        course.setCourseId(dto.getCourseId());
                    }

                    // 更新字段
                    course.setClazzId(dto.getClazzId());
                    course.setCpi(dto.getCpi());
                    course.setCourseName(dto.getCourseName());
                    course.setTeacherName(dto.getTeacherName());
                    course.setSchoolName(dto.getSchoolName());
                    course.setDescription(dto.getDescription());
                    course.setCoverUrl(dto.getCoverUrl());
                    course.setStatus(dto.getStatus());
                    course.setSyncTime(now);

                    return course;
                })
                .collect(Collectors.toList());

        // 批量保存
        List<Course> result = courseRepository.saveAll(savedCourses);
        log.info("课程列表同步完成，共{}门课程", result.size());

        return result;
    }

    /**
     * 获取或创建课程记录
     *
     * @param courseId 课程ID
     * @param clazzId 班级ID
     * @param cpi CPI
     * @param courseName 课程名称
     * @return 课程实体
     */
    @Transactional
    public Course getOrCreateCourse(String courseId, String clazzId, String cpi, String courseName) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("用户未登录");
        }

        return courseRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseGet(() -> {
                    Course course = new Course();
                    course.setUserId(userId);
                    course.setCourseId(courseId);
                    course.setClazzId(clazzId);
                    course.setCpi(cpi);
                    course.setCourseName(courseName);
                    course.setSyncTime(LocalDateTime.now());
                    return courseRepository.save(course);
                });
    }

    /**
     * 更新课程学习进度
     *
     * @param courseId 课程ID
     * @param progress 进度百分比
     * @param totalJobs 总任务数
     * @param completedJobs 已完成任务数
     */
    @Transactional
    public void updateCourseProgress(String courseId, int progress, int totalJobs, int completedJobs) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            log.warn("用户未登录，无法更新进度");
            return;
        }

        courseRepository.findByUserIdAndCourseId(userId, courseId).ifPresent(course -> {
            course.setProgress(progress);
            course.setTotalJobs(totalJobs);
            course.setCompletedJobs(completedJobs);
            course.setUpdateTime(LocalDateTime.now());
            courseRepository.save(course);
            log.debug("更新课程进度: courseId={}, progress={}%", courseId, progress);
        });
    }

    /**
     * 检查课程是否需要同步
     *
     * @param courseId 课程ID
     * @return true如果需要同步
     */
    public boolean needsSync(String courseId) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return true;
        }

        return courseRepository.findByUserIdAndCourseId(userId, courseId)
                .map(course -> {
                    if (course.getSyncTime() == null) {
                        return true;
                    }
                    LocalDateTime threshold = LocalDateTime.now().minusHours(SYNC_INTERVAL_HOURS);
                    return course.getSyncTime().isBefore(threshold);
                })
                .orElse(true);
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        // 这里假设UserDetails中存储的是用户ID
        try {
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException e) {
            log.warn("无法解析用户ID: {}", authentication.getName());
            return null;
        }
    }
}
