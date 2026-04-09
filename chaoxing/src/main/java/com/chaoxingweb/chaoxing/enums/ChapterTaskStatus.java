package com.chaoxingweb.chaoxing.enums;

/**
 * 章节任务状态枚举
 *
 * @author 小克 🐕💎
 * @since 2026-04-09
 */
public enum ChapterTaskStatus {

    /**
     * 待处理
     */
    PENDING,

    /**
     * 处理中
     */
    RUNNING,

    /**
     * 成功完成
     */
    SUCCESS,

    /**
     * 失败
     */
    ERROR,

    /**
     * 章节未开放（需要重试或跳过）
     */
    NOT_OPEN,

    /**
     * 已跳过
     */
    SKIPPED,

    /**
     * 超过最大重试次数
     */
    MAX_RETRY_EXCEEDED
}
