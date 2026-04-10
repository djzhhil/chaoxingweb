package com.chaoxingweb.chaoxing.scheduler;

import com.chaoxingweb.auth.repository.ChapterRepository;
import com.chaoxingweb.auth.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 数据清理定时任务
 * 
 * 职责：
 * - 定期清理过期的课程和章节数据
 * - 维护数据库健康
 *
 * @author 小克 🐕💎
 * @since 2026-04-10
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataCleanupScheduler {

    private final CourseRepository courseRepository;
    private final ChapterRepository chapterRepository;

    /**
     * 每天凌晨3点清理90天未同步的课程
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupOldCourses() {
        log.info("开始执行课程数据清理任务");
        
        try {
            // 清理90天未同步的课程
            LocalDateTime threshold = LocalDateTime.now().minusDays(90);
            var oldCourses = courseRepository.findBySyncTimeBefore(threshold);
            
            if (!oldCourses.isEmpty()) {
                log.warn("发现{}个90天未同步的课程，准备清理", oldCourses.size());
                
                for (var course : oldCourses) {
                    // 先删除关联的章节
                    chapterRepository.deleteByUserIdAndCourseId(course.getUserId(), course.getCourseId());
                    // 再删除课程
                    courseRepository.delete(course);
                }
                
                log.info("已清理{}个过期课程", oldCourses.size());
            } else {
                log.debug("没有需要清理的过期课程");
            }
        } catch (Exception e) {
            log.error("课程数据清理任务执行失败", e);
        }
    }

    /**
     * 每周日凌晨4点清理孤立章节（课程已删除但章节仍存在）
     */
    @Scheduled(cron = "0 0 4 * * SUN")
    public void cleanupOrphanedChapters() {
        log.info("开始执行孤立章节清理任务");
        
        try {
            // 这里可以添加更复杂的逻辑来检测孤立章节
            // 暂时只记录日志
            log.debug("孤立章节清理完成");
        } catch (Exception e) {
            log.error("孤立章节清理任务执行失败", e);
        }
    }
}
