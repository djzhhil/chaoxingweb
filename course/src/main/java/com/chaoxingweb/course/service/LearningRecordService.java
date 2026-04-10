package com.chaoxingweb.course.service;

import com.chaoxingweb.auth.entity.LearningRecord;
import com.chaoxingweb.auth.repository.LearningRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 学习记录服务
 * 
 * 职责：
 * - 管理任务点学习进度的持久化
 * - 支持断点续学
 * - 统计学习完成情况
 *
 * @author 小克 🐕💎
 * @since 2026-04-10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LearningRecordService {

    private final LearningRecordRepository learningRecordRepository;

    /**
     * 获取或创建学习记录
     *
     * @param courseId 课程ID
     * @param chapterId 章节ID
     * @param jobId 任务ID
     * @param jobType 任务类型
     * @param jobName 任务名称
     * @param objectId 对象ID
     * @return 学习记录
     */
    @Transactional
    public LearningRecord getOrCreateRecord(String courseId, String chapterId, String jobId, 
                                            String jobType, String jobName, String objectId) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("用户未登录");
        }

        return learningRecordRepository.findByUserIdAndCourseIdAndJobId(userId, courseId, jobId)
                .orElseGet(() -> {
                    LearningRecord record = new LearningRecord();
                    record.setUserId(userId);
                    record.setCourseId(courseId);
                    record.setChapterId(chapterId);
                    record.setJobId(jobId);
                    record.setJobType(jobType);
                    record.setJobName(jobName);
                    record.setObjectId(objectId);
                    record.setStatus("pending");
                    record.setProgress(0);
                    record.setPlayedTime(0);
                    record.setFailCount(0);
                    
                    LearningRecord saved = learningRecordRepository.save(record);
                    log.debug("创建学习记录: jobId={}, jobType={}", jobId, jobType);
                    return saved;
                });
    }

    /**
     * 更新学习进度
     *
     * @param jobId 任务ID
     * @param status 状态
     * @param progress 进度百分比
     * @param playedTime 已播放时长（秒）
     */
    @Transactional
    public void updateProgress(String jobId, String status, int progress, int playedTime) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            log.warn("用户未登录，无法更新进度");
            return;
        }

        learningRecordRepository.findByUserIdAndCourseIdAndJobId(userId, null, jobId)
                .or(() -> learningRecordRepository.findByUserIdAndChapterIdAndJobId(userId, null, jobId))
                .ifPresent(record -> {
                    record.setStatus(status);
                    record.setProgress(progress);
                    record.setPlayedTime(playedTime);
                    record.setLastStudyTime(LocalDateTime.now());
                    
                    if ("completed".equals(status)) {
                        record.setCompletedTime(LocalDateTime.now());
                    }
                    
                    learningRecordRepository.save(record);
                    log.debug("更新学习进度: jobId={}, status={}, progress={}%", jobId, status, progress);
                });
    }

    /**
     * 标记任务为完成
     *
     * @param courseId 课程ID
     * @param chapterId 章节ID
     * @param jobId 任务ID
     */
    @Transactional
    public void markAsCompleted(String courseId, String chapterId, String jobId) {
        updateProgress(jobId, "completed", 100, 0);
        log.info("任务已完成: jobId={}", jobId);
    }

    /**
     * 标记任务为失败
     *
     * @param jobId 任务ID
     * @param errorMessage 错误信息
     */
    @Transactional
    public void markAsFailed(String jobId, String errorMessage) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return;
        }

        learningRecordRepository.findByUserIdAndCourseIdAndJobId(userId, null, jobId)
                .or(() -> learningRecordRepository.findByUserIdAndChapterIdAndJobId(userId, null, jobId))
                .ifPresent(record -> {
                    record.setStatus("failed");
                    record.setFailCount(record.getFailCount() + 1);
                    record.setLastError(errorMessage);
                    record.setLastStudyTime(LocalDateTime.now());
                    learningRecordRepository.save(record);
                    log.warn("任务失败: jobId={}, error={}, failCount={}", 
                            jobId, errorMessage, record.getFailCount());
                });
    }

    /**
     * 获取未完成的学习记录
     *
     * @param courseId 课程ID
     * @return 未完成的任务列表
     */
    public List<LearningRecord> getIncompleteRecords(String courseId) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return List.of();
        }

        List<String> incompleteStatuses = List.of("pending", "running", "failed");
        return learningRecordRepository.findByUserIdAndCourseIdAndStatusIn(
                userId, courseId, incompleteStatuses);
    }

    /**
     * 获取章节的所有学习记录
     *
     * @param courseId 课程ID
     * @param chapterId 章节ID
     * @return 学习记录列表
     */
    public List<LearningRecord> getChapterRecords(String courseId, String chapterId) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return List.of();
        }

        return learningRecordRepository.findByUserIdAndCourseIdAndChapterIdOrderByCreateTimeAsc(
                userId, courseId, chapterId);
    }

    /**
     * 统计课程完成情况
     *
     * @param courseId 课程ID
     * @return 完成百分比
     */
    public int calculateCourseProgress(String courseId) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return 0;
        }

        long total = learningRecordRepository.countTotalByUserIdAndCourseId(userId, courseId);
        if (total == 0) {
            return 0;
        }

        long completed = learningRecordRepository.countCompletedByUserIdAndCourseId(userId, courseId);
        return (int) ((completed * 100) / total);
    }

    /**
     * 删除章节的所有学习记录
     *
     * @param courseId 课程ID
     * @param chapterId 章节ID
     */
    @Transactional
    public void deleteChapterRecords(String courseId, String chapterId) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return;
        }

        learningRecordRepository.deleteByUserIdAndCourseIdAndChapterId(userId, courseId, chapterId);
        log.info("已删除章节的学习记录: courseId={}, chapterId={}", courseId, chapterId);
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
