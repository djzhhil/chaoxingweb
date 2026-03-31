package com.chaoxingweb.chaoxing.facade.impl;

import com.chaoxingweb.auth.service.LoginService;
import com.chaoxingweb.chaoxing.converter.CourseConverter;
import com.chaoxingweb.chaoxing.course.CourseService;
import com.chaoxingweb.chaoxing.dto.ChaoxingLoginDTO;
import com.chaoxingweb.chaoxing.dto.CourseDTO;
import com.chaoxingweb.chaoxing.facade.ChaoxingFacade;
import com.chaoxingweb.chaoxing.vo.ChaoxingLoginResult;
import com.chaoxingweb.chaoxing.vo.CourseVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 超星模块 Facade 实现
 *
 * 职责：
 * - 对外提供统一接口
 * - 协调各个模块
 * - 不包含具体业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChaoxingFacadeImpl implements ChaoxingFacade {

    private final LoginService loginService;
    private final CourseService courseService;
    private final CourseConverter courseConverter;

    @Override
    public ChaoxingLoginResult login(ChaoxingLoginDTO dto) {
        log.info("超星登录请求: username={}", dto.getUsername());

        try {
            // 调用登录服务
            LoginService.LoginResult result;

            if (dto.isUseCookie()) {
                // Cookie 登录
                result = loginService.loginWithCookie(dto.getCookie());
            } else {
                // 账号密码登录
                result = loginService.loginWithPassword(dto.getUsername(), dto.getPassword());
            }

            // 转换为 VO
            if (result.isSuccess()) {
                return ChaoxingLoginResult.success(
                        result.getToken(),
                        result.getMessage(),
                        result.getCookie()
                );
            } else {
                return ChaoxingLoginResult.failure(result.getMessage());
            }
        } catch (Exception e) {
            log.error("超星登录失败: {}", e.getMessage(), e);
            return ChaoxingLoginResult.failure("超星登录失败: " + e.getMessage());
        }
    }

    @Override
    public List<CourseVO> getCourseList() {
        log.info("开始获取课程列表");

        try {
            // 调用课程服务
            List<CourseDTO> courseDTOs = courseService.getCourseList();

            // 使用转换器转换为 VO
            List<CourseVO> courseVOs = courseDTOs.stream()
                    .map(courseConverter::toVO)
                    .collect(Collectors.toList());

            log.info("课程列表获取成功，共{}门课程", courseVOs.size());
            return courseVOs;

        } catch (Exception e) {
            log.error("获取课程列表失败", e);
            throw new RuntimeException("获取课程列表失败: " + e.getMessage());
        }
    }

    @Override
    public CourseVO getCourseDetail(String courseId) {
        log.info("开始获取课程详情: courseId={}", courseId);

        try {
            // 调用课程服务
            CourseDTO courseDTO = courseService.getCourseDetail(courseId);

            // 使用转换器转换为 VO
            CourseVO courseVO = courseConverter.toVO(courseDTO);

            log.info("课程详情获取成功: courseId={}", courseId);
            return courseVO;

        } catch (Exception e) {
            log.error("获取课程详情失败: courseId={}", courseId, e);
            throw new RuntimeException("获取课程详情失败: " + e.getMessage());
        }
    }
}
