package com.chaoxingweb.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 章节实体 - 用于持久化章节结构
 *
 * @author 小克 🐕💎
 * @since 2026-04-10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cx_chapter", indexes = {
        @Index(name = "idx_course_chapter", columnList = "courseId,chapterId"),
        @Index(name = "idx_user_chapter", columnList = "userId,courseId")
})
public class Chapter {

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
     * 章节ID（知识点ID）
     */
    @Column(nullable = false, length = 50)
    private String chapterId;

    /**
     * 章节标题
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * 父章节ID
     */
    @Column(length = 50)
    private String parentId;

    /**
     * 层级（从1开始）
     */
    @Builder.Default
    private Integer level = 1;

    /**
     * 章节状态（active/locked/completed）
     */
    @Column(length = 20)
    private String status;

    /**
     * 任务点数量
     */
    @Builder.Default
    private Integer jobCount = 0;

    /**
     * 是否已完成
     */
    @Builder.Default
    private Boolean hasFinished = false;

    /**
     * 是否需要解锁
     */
    @Builder.Default
    private Boolean needUnlock = false;

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
