package com.chaoxingweb.auth.repository;

import com.chaoxingweb.auth.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 章节数据访问层
 *
 * @author 小克 🐕💎
 * @since 2026-04-10
 */
@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {

    /**
     * 根据用户ID和课程ID查询所有章节
     */
    List<Chapter> findByUserIdAndCourseIdOrderByLevelAscCreateTimeAsc(Long userId, String courseId);

    /**
     * 根据用户ID、课程ID和章节ID查询
     */
    Optional<Chapter> findByUserIdAndCourseIdAndChapterId(Long userId, String courseId, String chapterId);

    /**
     * 查询需要同步的章节（超过指定时间未同步）
     */
    List<Chapter> findBySyncTimeBefore(LocalDateTime syncTime);

    /**
     * 删除课程的所有章节
     */
    void deleteByUserIdAndCourseId(Long userId, String courseId);

    /**
     * 统计课程的章节数量
     */
    long countByUserIdAndCourseId(Long userId, String courseId);

    /**
     * 查询已完成的任务点
     */
    List<Chapter> findByUserIdAndCourseIdAndHasFinishedTrue(Long userId, String courseId);
}
