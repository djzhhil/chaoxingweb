package com.chaoxingweb.chaoxing.dto;

import com.chaoxingweb.chaoxing.enums.ChapterTaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 章节任务追踪DTO
 * 
 * 用于跟踪每个章节的学习状态和进度
 *
 * @author 小克 🐕💎
 * @since 2026-04-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChapterTaskDTO {

    /**
     * 章节索引（从0开始）
     */
    private int index;

    /**
     * 章节ID
     */
    private String chapterId;

    /**
     * 章节名称
     */
    private String chapterName;

    /**
     * 知识点ID
     */
    private String knowledgeId;

    /**
     * 当前状态
     */
    @Builder.Default
    private ChapterTaskStatus status = ChapterTaskStatus.PENDING;

    /**
     * 已重试次数
     */
    @Builder.Default
    private int tries = 0;

    /**
     * 最大重试次数
     */
    @Builder.Default
    private int maxTries = 5;

    /**
     * 最后错误消息
     */
    private String lastError;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 完成时间
     */
    private LocalDateTime endTime;

    /**
     * 任务数量
     */
    private int totalJobs;

    /**
     * 已完成任务数
     */
    private int completedJobs;

    /**
     * 是否未开放
     */
    @Builder.Default
    private boolean notOpen = false;

    /**
     * 学习结果详情
     */
    private String resultMessage;

    /**
     * 标记为运行中
     */
    public void markRunning() {
        this.status = ChapterTaskStatus.RUNNING;
        this.startTime = LocalDateTime.now();
        this.tries++;
    }

    /**
     * 标记为成功
     */
    public void markSuccess(String message) {
        this.status = ChapterTaskStatus.SUCCESS;
        this.endTime = LocalDateTime.now();
        this.resultMessage = message;
    }

    /**
     * 标记为失败
     */
    public void markError(String error) {
        this.status = ChapterTaskStatus.ERROR;
        this.lastError = error;
        this.endTime = LocalDateTime.now();
    }

    /**
     * 标记为未开放
     */
    public void markNotOpen() {
        this.notOpen = true;
        this.status = ChapterTaskStatus.NOT_OPEN;
        this.lastError = "章节未开放";
    }

    /**
     * 是否可以重试
     *
     * @return 是否可以重试
     */
    public boolean canRetry() {
        return this.tries < this.maxTries && 
               (this.status == ChapterTaskStatus.ERROR || 
                this.status == ChapterTaskStatus.NOT_OPEN);
    }

    /**
     * 获取进度百分比
     *
     * @return 进度百分比（0-100）
     */
    public double getProgress() {
        if (totalJobs == 0) {
            return 100.0;
        }
        return (double) completedJobs / totalJobs * 100;
    }

    /**
     * 是否已完成（成功或跳过）
     *
     * @return 是否完成
     */
    public boolean isFinished() {
        return this.status == ChapterTaskStatus.SUCCESS || 
               this.status == ChapterTaskStatus.SKIPPED ||
               this.status == ChapterTaskStatus.MAX_RETRY_EXCEEDED;
    }
}
