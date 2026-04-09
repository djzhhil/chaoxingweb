package com.chaoxingweb.chaoxing.service;

import com.chaoxingweb.chaoxing.dto.ChapterTaskDTO;
import com.chaoxingweb.chaoxing.dto.LearningProgressDTO;
import com.chaoxingweb.chaoxing.enums.ChapterTaskStatus;
import com.chaoxingweb.chaoxing.scheduler.ChapterScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 课程学习服务
 * 
 * 职责：
 * - 管理整个课程的学习流程
 * - 集成章节调度器和进度推送
 * - 提供学习统计和报告
 *
 * @author 小克 🐕💎
 * @since 2026-04-09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseLearningService {

    private final StudyProgressService progressService;

    /**
     * 学习整个课程（所有章节）
     *
     * @param courseId 课程ID
     * @param courseName 课程名称
     * @param chapterJobsProvider 章节任务提供者函数
     * @return 学习进度汇总
     */
    public LearningProgressDTO studyCourse(String courseId, String courseName, 
                                           ChapterJobsProvider chapterJobsProvider) {
        log.info("═══════════════════════════════════════");
        log.info("🚀 开始学习课程: {} ({})", courseName, courseId);
        log.info("═══════════════════════════════════════");

        // 1. 初始化学习进度
        LearningProgressDTO progress = new LearningProgressDTO();
        progress.setCourseId(courseId);
        progress.setCourseName(courseName);

        // 2. 获取所有章节列表
        List<Map<String, Object>> chapters = chapterJobsProvider.getChapters();
        
        if (chapters == null || chapters.isEmpty()) {
            log.warn("课程没有章节，跳过学习");
            return progress;
        }

        int totalChapters = chapters.size();
        progress.setTotalChapters(totalChapters);
        
        log.info("📚 课程共有{}个章节", totalChapters);

        // 3. 创建章节任务列表
        List<ChapterTaskDTO> chapterTasks = new ArrayList<>();
        for (int i = 0; i < chapters.size(); i++) {
            Map<String, Object> chapter = chapters.get(i);
            
            ChapterTaskDTO task = new ChapterTaskDTO();
            task.setIndex(i);
            task.setChapterId((String) chapter.get("chapterId"));
            task.setChapterName((String) chapter.get("chapterName"));
            task.setStatus(ChapterTaskStatus.PENDING);
            task.setMaxTries(5); // 最多重试5次
            
            chapterTasks.add(task);
        }

        progress.setChapterTasks(chapterTasks);

        // 4. 创建章节调度器（并发数3）
        ChapterScheduler scheduler = new ChapterScheduler(3, progress);

        try {
            // 5. 执行章节学习
            scheduler.execute(chapterTasks, (task) -> {
                // 推送章节开始学习事件
                pushChapterProgress(courseId, task, "STARTED", "开始学习");

                try {
                    // 调用提供者学习章节
                    chapterJobsProvider.studyChapter(task.getIndex());
                    
                    // 标记成功
                    task.markSuccess("学习完成");
                    pushChapterProgress(courseId, task, "SUCCESS", "学习完成");
                    
                } catch (Exception e) {
                    log.error("章节学习失败: {}", task.getChapterName(), e);
                    task.markError(e.getMessage());
                    pushChapterProgress(courseId, task, "ERROR", e.getMessage());
                    
                    // 如果还可以重试，抛出异常让调度器处理
                    if (task.canRetry()) {
                        throw e;
                    } else {
                        task.setStatus(ChapterTaskStatus.MAX_RETRY_EXCEEDED);
                    }
                }
                
                // 更新统计信息
                progress.updateStatistics();
                
                // 推送整体进度
                pushCourseProgress(courseId, progress);
            });

            // 6. 学习完成，生成报告
            progress.setEndTime(java.time.LocalDateTime.now());
            progress.updateStatistics();
            
            String report = progress.generateReport();
            log.info("\n{}", report);

            return progress;

        } finally {
            // 7. 关闭调度器
            scheduler.shutdown();
            
            // 8. 关闭SSE连接
            progressService.closeCourseConnection(courseId);
        }
    }

    /**
     * 推送课程进度
     *
     * @param courseId 课程ID
     * @param progress 学习进度
     */
    private void pushCourseProgress(String courseId, LearningProgressDTO progress) {
        try {
            progressService.pushCourseProgress(courseId, progress);
        } catch (Exception e) {
            log.warn("推送课程进度失败", e);
        }
    }

    /**
     * 推送章节进度
     *
     * @param courseId 课程ID
     * @param task 章节任务
     * @param status 状态
     * @param message 消息
     */
    private void pushChapterProgress(String courseId, ChapterTaskDTO task, 
                                     String status, String message) {
        try {
            progressService.pushChapterProgress(
                    courseId, 
                    task.getIndex(), 
                    task.getChapterName(), 
                    status, 
                    message
            );
        } catch (Exception e) {
            log.warn("推送章节进度失败", e);
        }
    }

    /**
     * 章节任务提供者接口
     */
    public interface ChapterJobsProvider {
        /**
         * 获取所有章节列表
         *
         * @return 章节列表，每个章节包含chapterId和chapterName
         */
        List<Map<String, Object>> getChapters();

        /**
         * 学习单个章节
         *
         * @param chapterIndex 章节索引
         */
        void studyChapter(int chapterIndex);
    }
}
