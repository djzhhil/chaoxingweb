package com.chaoxingweb.chaoxing.util;

import lombok.extern.slf4j.Slf4j;

/**
 * 进度条工具类
 * 用于显示任务执行进度
 *
 * @author 小克 🐕💎
 * @since 2026-04-08
 */
@Slf4j
public class ProgressBar {

    private static final int BAR_LENGTH = 40; // 进度条长度

    /**
     * 将秒数转换为时分秒格式
     *
     * @param seconds 秒数
     * @return 格式化的时间字符串，如 "1:23:45" 或 "23:45" 或 "--:--"
     */
    public static String sec2time(int seconds) {
        if (seconds <= 0) {
            return "--:--";
        }

        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, secs);
        } else {
            return String.format("%02d:%02d", minutes, secs);
        }
    }

    /**
     * 显示进度条（使用日志输出）
     *
     * @param taskName 任务名称
     * @param currentPosition 当前位置（秒）
     * @param totalLength 总长度（秒）
     * @param percentComplete 完成百分比
     */
    public static void showProgress(String taskName, int currentPosition, int totalLength, int percentComplete) {
        // 生成进度条
        int filledLength = (int) ((long) percentComplete * BAR_LENGTH / 100);
        StringBuilder progressBar = new StringBuilder();
        
        for (int i = 0; i < BAR_LENGTH; i++) {
            if (i < filledLength) {
                progressBar.append("#");
            } else {
                progressBar.append("-");
            }
        }

        // 格式化输出
        String progressText = String.format(
                "当前任务: %s |%s| %d%% %s/%s",
                taskName,
                progressBar.toString(),
                percentComplete,
                sec2time(currentPosition),
                sec2time(totalLength)
        );

        // 使用INFO级别日志输出
        log.info(progressText);
    }

    /**
     * 显示完成消息
     *
     * @param taskName 任务名称
     */
    public static void showComplete(String taskName) {
        System.out.println(); // 换行
        log.info("✅ 任务完成: {}", taskName);
    }
}
