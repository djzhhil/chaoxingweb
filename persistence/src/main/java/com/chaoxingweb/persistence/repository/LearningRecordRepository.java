package com.chaoxingweb.persistence.repository;

import com.chaoxingweb.persistence.entity.LearningRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 学习记录数据访问层
 *
 * @author 小克 🐕💎
 * @since 2026-04-10
 */
@Repository
public interface LearningRecordRepository extends JpaRepository<LearningRecord, Long> {

    /**
     * 根据用户ID、课程ID和任务ID查询
     */
    Optional<LearningRecord> findByUserIdAndCourseIdAndJobId(Long userId, String courseId, String jobId);

    /**
     * 根据用户ID、章节ID和任务ID查询
     */
    Optional<LearningRecord> findByUserIdAndChapterIdAndJobId(Long userId, String chapterId, String jobId);

    /**
     * 查询章节的所有学习记录
     */
    List<LearningRecord> findByUserIdAndCourseIdAndChapterIdOrderByCreateTimeAsc(
            Long userId, String courseId, String chapterId);

    /**
     * 查询课程的所有学习记录
     */
    List<LearningRecord> findByUserIdAndCourseIdOrderByChapterIdAscCreateTimeAsc(
            Long userId, String courseId);

    /**
     * 查询未完成的学习记录
     */
    List<LearningRecord> findByUserIdAndCourseIdAndStatusIn(
            Long userId, String courseId, List<String> statuses);

    /**
     * 统计课程的完成情况
     */
    @Query("SELECT COUNT(lr) FROM LearningRecord lr WHERE lr.userId = :userId AND lr.courseId = :courseId AND lr.status = 'completed'")
    long countCompletedByUserIdAndCourseId(Long userId, String courseId);

    /**
     * 统计课程的总任务数
     */
    @Query("SELECT COUNT(lr) FROM LearningRecord lr WHERE lr.userId = :userId AND lr.courseId = :courseId")
    long countTotalByUserIdAndCourseId(Long userId, String courseId);

    /**
     * 删除章节的所有学习记录
     */
    void deleteByUserIdAndCourseIdAndChapterId(Long userId, String courseId, String chapterId);

    /**
     * 删除课程的所有学习记录
     */
    void deleteByUserIdAndCourseId(Long userId, String courseId);
}
