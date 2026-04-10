package com.chaoxingweb.auth.repository;

import com.chaoxingweb.auth.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 课程数据访问层
 *
 * @author 小克 🐕💎
 * @since 2026-04-10
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    /**
     * 根据用户ID和课程ID查询
     */
    Optional<Course> findByUserIdAndCourseId(Long userId, String courseId);

    /**
     * 查询用户的所有课程
     */
    List<Course> findByUserIdOrderByUpdateTimeDesc(Long userId);

    /**
     * 查询需要同步的课程（超过指定时间未同步）
     */
    List<Course> findBySyncTimeBefore(LocalDateTime syncTime);

    /**
     * 删除用户的课程
     */
    void deleteByUserIdAndCourseId(Long userId, String courseId);

    /**
     * 统计用户的课程数量
     */
    long countByUserId(Long userId);
}
