package com.chaoxingweb.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 学习记录实体 - 用于持久化任务点学习进度
 *
 * @author 小克 🐕💎
 * @since 2026-04-10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cx_learning_record", indexes = {
        @Index(name = "idx_user_job", columnList = "userId,jobId"),
        @Index(name = "idx_user_chapter_job", columnList = "userId,chapterId,jobId")
})
public class LearningRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户ID
     */
    @Column(nullable = false)
    private Long userId;

    /**
     * 课程ID
     */
    @Column(nullable = false, length = 50)
    private String courseId;

    /**
     * 章节ID（知识点ID）
     */
    @Column(nullable = false, length = 50)
    private String chapterId;

    /**
     * 任务ID
     */
    @Column(nullable = false, length = 50)
    private String jobId;

    /**
     * 任务类型（video/document/read/work/live/empty_page）
     */
    @Column(nullable = false, length = 20)
    private String jobType;

    /**
     * 任务名称
     */
    @Column(length = 200)
    private String jobName;

    /**
     * 对象ID（视频/文档的objectId）
     */
    @Column(length = 100)
    private String objectId;

    /**
     * 学习状态（pending/running/completed/failed/skipped）
     */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "pending";

    /**
     * 学习进度（百分比，0-100）
     */
    @Builder.Default
    private Integer progress = 0;

    /**
     * 已播放时长（秒，针对视频/音频）
     */
    @Builder.Default
    private Integer playedTime = 0;

    /**
     * 总时长（秒）
     */
    private Integer duration;

    /**
     * 最后学习时间
     */
    private LocalDateTime lastStudyTime;

    /**
     * 完成时间
     */
    private LocalDateTime completedTime;

    /**
     * 失败次数
     */
    @Builder.Default
    private Integer failCount = 0;

    /**
     * 最后错误信息
     */
    @Column(columnDefinition = "TEXT")
    private String lastError;

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
