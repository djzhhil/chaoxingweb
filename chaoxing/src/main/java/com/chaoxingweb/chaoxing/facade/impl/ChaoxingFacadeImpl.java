package com.chaoxingweb.chaoxing.facade.impl;

import com.chaoxingweb.auth.service.LoginService;
import com.chaoxingweb.chaoxing.converter.ChapterConverter;
import com.chaoxingweb.chaoxing.converter.CourseConverter;
import com.chaoxingweb.chaoxing.core.CacheManager;
import com.chaoxingweb.chaoxing.course.ChaoxingChapterService;
import com.chaoxingweb.chaoxing.course.ChaoxingCourseService;
import com.chaoxingweb.chaoxing.dto.ChaoxingLoginDTO;
import com.chaoxingweb.chaoxing.dto.ChapterDTO;
import com.chaoxingweb.chaoxing.dto.CourseDTO;
import com.chaoxingweb.chaoxing.facade.ChaoxingFacade;
import com.chaoxingweb.chaoxing.vo.ChaoxingLoginResult;
import com.chaoxingweb.chaoxing.vo.ChapterVO;
import com.chaoxingweb.chaoxing.vo.CourseVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final ChaoxingCourseService chaoxingCourseService;
    private final ChaoxingChapterService chaoxingChapterService;
    private final CourseConverter courseConverter;
    private final ChapterConverter chapterConverter;
    private final CacheManager cacheManager;

    // 缓存过期时间配置（分钟）
    private static final long COURSE_LIST_CACHE_TTL = 30; // 课程列表缓存30分钟
    private static final long CHAPTER_LIST_CACHE_TTL = 30; // 章节列表缓存30分钟

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
            // 尝试从缓存获取
            String cacheKey = "course:list";
            List<CourseVO> cachedCourses = cacheManager.get(cacheKey, List.class);
            if (cachedCourses != null) {
                log.info("从缓存中获取课程列表，共{}门课程", cachedCourses.size());
                return cachedCourses;
            }

            // 缓存未命中，调用课程服务
            List<CourseDTO> courseDTOs = chaoxingCourseService.getCourseList();

            // 使用转换器转换为 VO
            List<CourseVO> courseVOs = courseDTOs.stream()
                    .map(courseConverter::toVO)
                    .collect(Collectors.toList());

            // 存入缓存
            cacheManager.put(cacheKey, courseVOs, COURSE_LIST_CACHE_TTL);

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
            CourseDTO courseDTO = chaoxingCourseService.getCourseDetail(courseId);

            // 使用转换器转换为 VO
            CourseVO courseVO = courseConverter.toVO(courseDTO);

            log.info("课程详情获取成功: courseId={}", courseId);
            return courseVO;

        } catch (Exception e) {
            log.error("获取课程详情失败: courseId={}", courseId, e);
            throw new RuntimeException("获取课程详情失败: " + e.getMessage());
        }
    }

    @Override
    public List<ChapterVO> getChapterList(String courseId, String clazzId, String cpi) {
        log.info("开始获取课程章节列表: courseId={}, clazzId={}, cpi={}", courseId, clazzId, cpi);

        try {
            // 尝试从缓存获取
            String cacheKey = String.format("chapter:list:%s:%s:%s", courseId, clazzId, cpi);
            List<ChapterVO> cachedChapters = cacheManager.get(cacheKey, List.class);
            if (cachedChapters != null) {
                log.info("从缓存中获取章节列表，共{}个章节", cachedChapters.size());
                return cachedChapters;
            }

            // 缓存未命中，调用章节服务
            List<ChapterDTO> chapterDTOs = chaoxingChapterService.getChapterList(courseId, clazzId, cpi);

            // 使用转换器转换为 VO
            List<ChapterVO> chapterVOs = chapterDTOs.stream()
                    .map(chapterConverter::toVO)
                    .collect(Collectors.toList());

            // 存入缓存
            cacheManager.put(cacheKey, chapterVOs, CHAPTER_LIST_CACHE_TTL);

            log.info("章节列表获取成功，共{}个章节", chapterVOs.size());
            return chapterVOs;

        } catch (Exception e) {
            log.error("获取章节列表失败: courseId={}, clazzId={}, cpi={}", courseId, clazzId, cpi, e);
            throw new RuntimeException("获取章节列表失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getChapterDetail(String courseId, String clazzId, String cpi) {
        log.info("开始获取课程章节详情: courseId={}, clazzId={}, cpi={}", courseId, clazzId, cpi);

        try {
            // 调用章节服务获取详细信息
            Map<String, Object> detail = chaoxingChapterService.getChapterDetail(courseId, clazzId, cpi);

            // 转换章节列表为 VO
            @SuppressWarnings("unchecked")
            List<ChapterDTO> chapterDTOs = (List<ChapterDTO>) detail.get("points");
            
            List<ChapterVO> chapterVOs = new ArrayList<>();
            if (chapterDTOs != null) {
                chapterVOs = chapterDTOs.stream()
                        .map(chapterConverter::toVO)
                        .collect(Collectors.toList());
            }

            // 计算统计信息
            int totalChapters = chapterVOs.size();
            long completedCount = chapterVOs.stream()
                    .filter(ch -> "completed".equals(ch.getStatus()))
                    .count();
            long lockedCount = chapterVOs.stream()
                    .filter(ch -> "locked".equals(ch.getStatus()))
                    .count();
            long activeCount = chapterVOs.stream()
                    .filter(ch -> "active".equals(ch.getStatus()))
                    .count();
            
            int progress = totalChapters > 0 ? (int) ((completedCount * 100) / totalChapters) : 0;

            // 构造返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("hasLocked", detail.get("hasLocked"));
            result.put("totalChapters", totalChapters);
            result.put("completedChapters", completedCount);
            result.put("lockedChapters", lockedCount);
            result.put("activeChapters", activeCount);
            result.put("progress", progress);
            result.put("chapters", chapterVOs);

            log.info("章节详情获取成功: courseId={}, clazzId={}, cpi={}, 进度={}%, 已完成={}/{}",
                    courseId, clazzId, cpi, progress, completedCount, totalChapters);
            return result;

        } catch (Exception e) {
            log.error("获取章节详情失败: courseId={}, clazzId={}, cpi={}", courseId, clazzId, cpi, e);
            throw new RuntimeException("获取章节详情失败: " + e.getMessage());
        }
    }
}
