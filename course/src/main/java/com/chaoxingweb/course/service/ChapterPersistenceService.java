package com.chaoxingweb.course.service;

import com.chaoxingweb.persistence.entity.Chapter;
import com.chaoxingweb.persistence.repository.ChapterRepository;
import com.chaoxingweb.chaoxing.dto.ChapterDTO;
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
 * 章节持久化服务
 * 
 * 职责：
 * - 管理章节的数据库持久化
 * - 提供缓存优先的查询策略
 * - 同步超星API数据到数据库
 *
 * @author 小克 🐕💎
 * @since 2026-04-10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChapterPersistenceService {

    private final ChapterRepository chapterRepository;

    /**
     * 章节同步间隔（小时）
     */
    private static final int SYNC_INTERVAL_HOURS = 7 * 24; // 7天

    /**
     * 获取课程的章节列表（从数据库读取）
     *
     * @param courseId 课程ID
     * @return 章节列表
     */
    public List<Chapter> getCourseChapters(String courseId) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            log.warn("用户未登录，无法获取章节列表");
            return List.of();
        }

        List<Chapter> chapters = chapterRepository.findByUserIdAndCourseIdOrderByLevelAscCreateTimeAsc(userId, courseId);
        log.debug("从数据库获取{}个章节", chapters.size());
        
        return chapters;
    }

    /**
     * 从API同步章节列表到数据库
     *
     * @param courseId 课程ID
     * @param clazzId 班级ID
     * @param cpi CPI
     * @param chapterDTOs API返回的章节列表
     * @return 同步后的章节列表
     */
    @Transactional
    public List<Chapter> syncChaptersFromApi(String courseId, String clazzId, String cpi, List<ChapterDTO> chapterDTOs) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            log.warn("用户未登录，无法同步章节");
            return List.of();
        }

        log.info("开始同步章节列表: courseId={}, 共{}个章节", courseId, chapterDTOs.size());

        LocalDateTime now = LocalDateTime.now();
        List<Chapter> savedChapters = chapterDTOs.stream()
                .map(dto -> {
                    // 查询是否已存在
                    Chapter existingChapter = chapterRepository
                            .findByUserIdAndCourseIdAndChapterId(userId, courseId, dto.getId())
                            .orElse(null);

                    Chapter chapter;
                    if (existingChapter != null) {
                        chapter = existingChapter;
                    } else {
                        chapter = new Chapter();
                        chapter.setUserId(userId);
                        chapter.setCourseId(courseId);
                        chapter.setChapterId(dto.getId());
                    }

                    // 更新字段
                    chapter.setClazzId(clazzId);
                    chapter.setCpi(cpi);
                    chapter.setTitle(dto.getTitle());
                    chapter.setParentId(dto.getParentId());
                    chapter.setLevel(dto.getLevel());
                    chapter.setStatus(dto.getStatus());
                    chapter.setJobCount(dto.getJobCount());
                    chapter.setHasFinished(dto.isHasFinished());
                    chapter.setNeedUnlock(dto.isNeedUnlock());
                    chapter.setSyncTime(now);

                    return chapter;
                })
                .collect(Collectors.toList());

        // 批量保存
        List<Chapter> result = chapterRepository.saveAll(savedChapters);
        log.info("章节列表同步完成，共{}个章节", result.size());

        return result;
    }

    /**
     * 检查章节是否需要同步
     *
     * @param courseId 课程ID
     * @return true如果需要同步
     */
    public boolean needsSync(String courseId) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return true;
        }

        // 检查该课程是否有任何章节
        long count = chapterRepository.countByUserIdAndCourseId(userId, courseId);
        if (count == 0) {
            return true;
        }

        // 检查最后同步时间
        List<Chapter> chapters = chapterRepository.findByUserIdAndCourseIdOrderByLevelAscCreateTimeAsc(userId, courseId);
        if (chapters.isEmpty()) {
            return true;
        }

        Chapter lastChapter = chapters.get(chapters.size() - 1);
        if (lastChapter.getSyncTime() == null) {
            return true;
        }

        LocalDateTime threshold = LocalDateTime.now().minusHours(SYNC_INTERVAL_HOURS);
        return lastChapter.getSyncTime().isBefore(threshold);
    }

    /**
     * 删除课程的所有章节（用于重新同步）
     *
     * @param courseId 课程ID
     */
    @Transactional
    public void deleteCourseChapters(String courseId) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return;
        }

        chapterRepository.deleteByUserIdAndCourseId(userId, courseId);
        log.info("已删除课程的所有章节: courseId={}", courseId);
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        try {
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException e) {
            log.warn("无法解析用户ID: {}", authentication.getName());
            return null;
        }
    }
}
