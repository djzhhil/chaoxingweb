package com.chaoxingweb.chaoxing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 学习进度DTO
 *
 * @author 小克 🐕💎
 * @since 2026-04-08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyProgress {
    private String jobId;           // 任务ID
    private String jobName;         // 任务名称
    private String jobType;         // 任务类型（VIDEO/DOCUMENT等）
    private int currentTime;        // 当前播放时间（秒）
    private int totalTime;          // 总时长（秒）
    private int percent;            // 完成百分比
    private String status;          // 状态：STUDYING/COMPLETED/FAILED
    private long timestamp;         // 时间戳
}
