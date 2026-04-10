package com.chaoxingweb.course.service.impl;

import com.chaoxingweb.persistence.entity.Course;
import com.chaoxingweb.persistence.entity.User;
import com.chaoxingweb.persistence.repository.CourseRepository;
import com.chaoxingweb.persistence.repository.UserRepository;
import com.chaoxingweb.chaoxing.core.CipherManager;
import com.chaoxingweb.chaoxing.core.SessionManager;
import com.chaoxingweb.chaoxing.dto.CourseDTO;
import com.chaoxingweb.chaoxing.facade.ChaoxingFacade;
import com.chaoxingweb.chaoxing.vo.CourseVO;
import com.chaoxingweb.course.service.CourseService;
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
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final SessionManager sessionManager;
    private final CipherManager cipherManager;

    /**
     * 课程同步间隔（小时）
     */
    private static final int SYNC_INTERVAL_HOURS = 24;

    @Override
    public List<CourseVO> getCourseList() {
        log.info("开始获取课程列表");

        try {
            // 1. 从数据库加载用户cookie并设置到SessionManager
            loadUserCookieToSession();

            // 2. 尝试从数据库读取课程列表
            Long userId = getCurrentUserId();
            if (userId != null) {
                List<Course> dbCourses = courseRepository.findByUserIdOrderByUpdateTimeDesc(userId);
                
                // 检查是否需要重新同步（超过24小时未同步）
                boolean needSync = dbCourses.isEmpty() || 
                    dbCourses.stream().anyMatch(c -> 
                        c.getSyncTime() == null || 
                        c.getSyncTime().isBefore(LocalDateTime.now().minusHours(SYNC_INTERVAL_HOURS))
                    );
                
                if (!needSync && !dbCourses.isEmpty()) {
                    log.info("从数据库获取课程列表，共{}门课程", dbCourses.size());
                    return convertToVO(dbCourses);
                }
                
                log.info("数据库课程需要同步或为空，将从API获取");
            }

            // 3. 调用 ChaoxingFacade 获取课程列表
            List<CourseVO> courses = chaoxingFacade.getCourseList();

            // 4. 同步到数据库
            syncCoursesToDatabase(courses);

            log.info("课程列表获取成功，共{}门课程", courses.size());
            return courses;

        } catch (Exception e) {
            log.error("获取课程列表失败", e);
            throw new RuntimeException("获取课程列表失败: " + e.getMessage());
        }
    }

    @Override
    public CourseVO getCourseDetail(String courseId) {
        log.info("开始获取课程详情: courseId={}", courseId);

        try {
            // 1. 从数据库加载用户cookie并设置到SessionManager
            loadUserCookieToSession();

            // 2. 调用 ChaoxingFacade 获取课程详情
            CourseVO course = chaoxingFacade.getCourseDetail(courseId);

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
            // 1. 从数据库加载用户cookie并设置到SessionManager
            loadUserCookieToSession();

            // 2. 调用 ChaoxingFacade 获取课程列表
            List<CourseVO> chaoxingCourses = chaoxingFacade.getCourseList();

            // 3. 同步到数据库
            syncCoursesToDatabase(chaoxingCourses);

            log.info("课程列表同步成功，共{}门课程", chaoxingCourses.size());
            return chaoxingCourses.size();

        } catch (Exception e) {
            log.error("同步课程列表失败", e);
            throw new RuntimeException("同步课程列表失败: " + e.getMessage());
        }
    }

    /**
     * 从数据库加载用户cookie并设置到SessionManager
     */
    private void loadUserCookieToSession() {
        try {
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("用户未登录，无法加载cookie");
                return;
            }

            String username = authentication.getName();
            log.debug("当前登录用户: {}", username);

            // 从数据库查询用户
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在: " + username));

            // 检查是否绑定了超星账号
            if (user.getChaoxingCookie() == null || user.getChaoxingCookie().isEmpty()) {
                log.warn("用户未绑定超星账号: {}", username);
                throw new RuntimeException("请先绑定超星账号");
            }

            // 解密 Cookie
            String decryptedCookie = cipherManager.decrypt(user.getChaoxingCookie());
            
            // 将cookie设置到SessionManager
            sessionManager.updateCookie(decryptedCookie);
            log.info("已从数据库加载并解密超星cookie到SessionManager: userId={}", user.getId());

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("加载用户cookie失败", e);
            throw new RuntimeException("加载用户cookie失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .map(User::getId)
                .orElse(null);
    }

    /**
     * 同步课程列表到数据库
     */
    @Transactional
    public void syncCoursesToDatabase(List<CourseVO> courseVOs) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            log.warn("用户未登录，无法同步课程");
            return;
        }

        log.info("开始同步{}门课程到数据库", courseVOs.size());

        LocalDateTime now = LocalDateTime.now();
        List<Course> courses = courseVOs.stream()
                .map(vo -> {
                    // 查询是否已存在
                    Course existingCourse = courseRepository
                            .findByUserIdAndCourseId(userId, vo.getCourseId())
                            .orElse(null);

                    Course course;
                    if (existingCourse != null) {
                        // 更新现有记录
                        course = existingCourse;
                    } else {
                        // 创建新记录
                        course = new Course();
                        course.setUserId(userId);
                        course.setCourseId(vo.getCourseId());
                    }

                    // 更新字段
                    course.setClazzId(vo.getClazzId());
                    course.setCpi(vo.getCpi());
                    course.setCourseName(vo.getCourseName());
                    course.setTeacherName(vo.getTeacherName());
                    course.setSchoolName(vo.getSchoolName());
                    course.setDescription(vo.getDescription());
                    course.setCoverUrl(vo.getCoverUrl());
                    course.setStatus(vo.getStatus());
                    course.setSyncTime(now);

                    return course;
                })
                .collect(Collectors.toList());

        // 批量保存
        courseRepository.saveAll(courses);
        log.info("成功同步{}门课程到数据库", courses.size());
    }

    /**
     * 将数据库Course实体转换为CourseVO
     */
    private List<CourseVO> convertToVO(List<Course> courses) {
        return courses.stream()
                .map(course -> {
                    CourseVO vo = new CourseVO();
                    vo.setCourseId(course.getCourseId());
                    vo.setClazzId(course.getClazzId());
                    vo.setCpi(course.getCpi());
                    vo.setCourseName(course.getCourseName());
                    vo.setTeacherName(course.getTeacherName());
                    vo.setSchoolName(course.getSchoolName());
                    vo.setDescription(course.getDescription());
                    vo.setCoverUrl(course.getCoverUrl());
                    vo.setStatus(course.getStatus());
                    return vo;
                })
                .collect(Collectors.toList());
    }
}
