package com.chaoxingweb.course.service.impl;

import com.chaoxingweb.auth.entity.User;
import com.chaoxingweb.auth.repository.UserRepository;
import com.chaoxingweb.chaoxing.core.SessionManager;
import com.chaoxingweb.chaoxing.vo.CourseVO;
import com.chaoxingweb.chaoxing.facade.ChaoxingFacade;
import com.chaoxingweb.course.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

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
    private final SessionManager sessionManager;

    @Override
    public List<CourseVO> getCourseList() {
        log.info("开始获取课程列表");

        try {
            // 1. 从数据库加载用户cookie并设置到SessionManager
            loadUserCookieToSession();

            // 2. 调用 ChaoxingFacade 获取课程列表
            List<CourseVO> courses = chaoxingFacade.getCourseList();

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

            // 将cookie设置到SessionManager
            sessionManager.updateCookie(user.getChaoxingCookie());
            log.info("已从数据库加载超星cookie到SessionManager: userId={}", user.getId());

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("加载用户cookie失败", e);
            throw new RuntimeException("加载用户cookie失败: " + e.getMessage());
        }
    }
}
