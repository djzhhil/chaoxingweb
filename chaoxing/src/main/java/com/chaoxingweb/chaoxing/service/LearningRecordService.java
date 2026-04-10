package com.chaoxingweb.chaoxing.service;

import com.chaoxingweb.persistence.entity.LearningRecord;
import com.chaoxingweb.persistence.repository.LearningRecordRepository;
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
 * 学习记录服务 - 管理任务点学习进度的持久化
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
            log.warn("用户未登录，无法创建学习记录");
            return null;
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
     * @param courseId 课程ID
     * @param jobId 任务ID
     * @param status 状态
     * @param progress 进度百分比
     * @param playedTime 已播放时长（秒）
     */
    @Transactional
    public void updateProgress(String courseId, String jobId, String status, int progress, int playedTime) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            log.warn("用户未登录，无法更新进度");
            return;
        }

        learningRecordRepository.findByUserIdAndCourseIdAndJobId(userId, courseId, jobId)
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
     * @param jobId 任务ID
     */
    @Transactional
    public void markAsCompleted(String courseId, String jobId) {
        updateProgress(courseId, jobId, "completed", 100, 0);
        log.info("任务已完成: jobId={}", jobId);
    }

    /**
     * 标记任务失败
     *
     * @param courseId 课程ID
     * @param jobId 任务ID
     * @param errorMessage 错误信息
     */
    @Transactional
    public void markAsFailed(String courseId, String jobId, String errorMessage) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return;
        }

        learningRecordRepository.findByUserIdAndCourseIdAndJobId(userId, courseId, jobId)
                .ifPresent(record -> {
                    record.setStatus("failed");
                    record.setLastError(errorMessage);
                    record.setFailCount(record.getFailCount() + 1);
                    record.setLastStudyTime(LocalDateTime.now());
                    learningRecordRepository.save(record);
                    log.warn("任务失败: jobId={}, error={}", jobId, errorMessage);
                });
    }

    /**
     * 获取上次学习进度（用于断点续学）
     *
     * @param courseId 课程ID
     * @param jobId 任务ID
     * @return 已播放时长（秒），如果没有记录返回0
     */
    public int getLastPlayTime(String courseId, String jobId) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return 0;
        }

        return learningRecordRepository.findByUserIdAndCourseIdAndJobId(userId, courseId, jobId)
                .map(LearningRecord::getPlayedTime)
                .orElse(0);
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
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        // 这里需要从UserRepository获取用户ID
        // 为了避免循环依赖，暂时返回null，由调用方处理
        log.warn("LearningRecordService需要注入UserRepository来获取用户ID");
        return null;
    }
}
