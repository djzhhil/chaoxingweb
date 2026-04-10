package com.chaoxingweb.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 课程实体 - 用于持久化课程信息
 *
 * @author 小克 🐕💎
 * @since 2026-04-10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cx_course", indexes = {
        @Index(name = "idx_course_id", columnList = "courseId"),
        @Index(name = "idx_user_course", columnList = "userId,courseId")
})
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户ID（关联User表）
     */
    @Column(nullable = false)
    private Long userId;

    /**
     * 课程ID
     */
    @Column(nullable = false, length = 50)
    private String courseId;

    /**
     * 班级ID
     */
    @Column(length = 50)
    private String clazzId;

    /**
     * CPI
     */
    @Column(length = 50)
    private String cpi;

    /**
     * 课程名称
     */
    @Column(nullable = false, length = 200)
    private String courseName;

    /**
     * 教师姓名
     */
    @Column(length = 100)
    private String teacherName;

    /**
     * 学校名称
     */
    @Column(length = 100)
    private String schoolName;

    /**
     * 课程描述
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * 封面URL
     */
    @Column(length = 500)
    private String coverUrl;

    /**
     * 课程状态
     */
    @Column(length = 20)
    private String status;

    /**
     * 学习进度（百分比）
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer progress = 0;

    /**
     * 总任务数
     */
    @Builder.Default
    private Integer totalJobs = 0;

    /**
     * 已完成任务数
     */
    @Builder.Default
    private Integer completedJobs = 0;

    /**
     * 最后同步时间
     */
    private LocalDateTime syncTime;

    /**
     * 创建时间
     */
    @Column(updatable = false)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
