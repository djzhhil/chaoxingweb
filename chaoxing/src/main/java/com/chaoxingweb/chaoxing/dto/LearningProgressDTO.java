package com.chaoxingweb.chaoxing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 学习进度汇总DTO
 * 
 * 用于跟踪整个课程的章节学习进度
 *
 * @author 小克 🐕💎
 * @since 2026-04-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningProgressDTO {

    /**
     * 课程ID
     */
    private String courseId;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 班级ID
     */
    private String clazzId;

    /**
     * 总章节数
     */
    private int totalChapters;

    /**
     * 已完成章节数
     */
    private int completedChapters;

    /**
     * 失败章节数
     */
    private int failedChapters;

    /**
     * 跳过章节数
     */
    private int skippedChapters;

    /**
     * 章节任务列表
     */
    @Builder.Default
    private List<ChapterTaskDTO> chapterTasks = new ArrayList<>();

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 当前正在处理的章节索引
     */
    private int currentChapterIndex = -1;

    /**
     * 是否正在运行
     */
    @Builder.Default
    private boolean running = false;

    /**
     * 添加章节任务
     *
     * @param task 章节任务
     */
    public void addChapterTask(ChapterTaskDTO task) {
        this.chapterTasks.add(task);
        this.totalChapters = this.chapterTasks.size();
    }

    /**
     * 更新统计信息
     */
    public void updateStatistics() {
        this.completedChapters = (int) chapterTasks.stream()
                .filter(task -> task.getStatus() == com.chaoxingweb.chaoxing.enums.ChapterTaskStatus.SUCCESS)
                .count();
        
        this.failedChapters = (int) chapterTasks.stream()
                .filter(task -> task.getStatus() == com.chaoxingweb.chaoxing.enums.ChapterTaskStatus.ERROR ||
                               task.getStatus() == com.chaoxingweb.chaoxing.enums.ChapterTaskStatus.MAX_RETRY_EXCEEDED)
                .count();
        
        this.skippedChapters = (int) chapterTasks.stream()
                .filter(task -> task.getStatus() == com.chaoxingweb.chaoxing.enums.ChapterTaskStatus.SKIPPED)
                .count();
    }

    /**
     * 获取总体进度百分比
     *
     * @return 进度百分比（0-100）
     */
    public double getOverallProgress() {
        if (totalChapters == 0) {
            return 0.0;
        }
        return (double) completedChapters / totalChapters * 100;
    }

    /**
     * 获取指定索引的章节任务
     *
     * @param index 章节索引
     * @return 章节任务，不存在返回null
     */
    public ChapterTaskDTO getChapterTask(int index) {
        if (index >= 0 && index < chapterTasks.size()) {
            return chapterTasks.get(index);
        }
        return null;
    }

    /**
     * 生成进度报告
     *
     * @return 进度报告字符串
     */
    public String generateReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════\n");
        sb.append("📊 学习进度报告\n");
        sb.append("═══════════════════════════════════════\n");
        sb.append(String.format("课程: %s\n", courseName != null ? courseName : "未知"));
        sb.append(String.format("总章节: %d\n", totalChapters));
        sb.append(String.format("✅ 成功: %d\n", completedChapters));
        sb.append(String.format("❌ 失败: %d\n", failedChapters));
        sb.append(String.format("⏭️  跳过: %d\n", skippedChapters));
        sb.append(String.format("📈 进度: %.1f%%\n", getOverallProgress()));
        sb.append("═══════════════════════════════════════\n");
        
        // 详细章节状态
        for (ChapterTaskDTO task : chapterTasks) {
            String statusIcon = switch (task.getStatus()) {
                case SUCCESS -> "✅";
                case ERROR -> "❌";
                case RUNNING -> "🔄";
                case PENDING -> "⏳";
                case NOT_OPEN -> "🔒";
                case SKIPPED -> "⏭️";
                case MAX_RETRY_EXCEEDED -> "⚠️";
            };
            
            sb.append(String.format("%s [%d/%d] %s - %s\n",
                    statusIcon,
                    task.getIndex() + 1,
                    totalChapters,
                    task.getChapterName() != null ? task.getChapterName() : "未知章节",
                    task.getStatus()));
            
            if (task.getLastError() != null && !task.getLastError().isEmpty()) {
                sb.append(String.format("   └─ 错误: %s\n", task.getLastError()));
            }
        }
        
        sb.append("═══════════════════════════════════════\n");
        
        return sb.toString();
    }
}
