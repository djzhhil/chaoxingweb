package com.chaoxingweb.chaoxing.scheduler;

import com.chaoxingweb.chaoxing.dto.ChapterTaskDTO;
import com.chaoxingweb.chaoxing.dto.LearningProgressDTO;
import com.chaoxingweb.chaoxing.enums.ChapterTaskStatus;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 章节调度器
 * 
 * 职责：
 * - 管理多章节并发学习
 * - 控制并发数量
 * - 处理失败重试
 * - 跟踪学习进度
 *
 * @author 小克 🐕💎
 * @since 2026-04-09
 */
@Slf4j
public class ChapterScheduler {

    private final ExecutorService executorService;
    private final int maxConcurrency;
    private final LearningProgressDTO progress;
    private final AtomicInteger activeThreads = new AtomicInteger(0);
    
    // 重试队列
    private final BlockingQueue<ChapterTaskDTO> retryQueue = new LinkedBlockingQueue<>();

    /**
     * 构造函数
     *
     * @param maxConcurrency 最大并发数
     * @param progress 学习进度对象
     */
    public ChapterScheduler(int maxConcurrency, LearningProgressDTO progress) {
        this.maxConcurrency = Math.max(1, maxConcurrency);
        this.progress = progress;
        
        // 创建线程池
        this.executorService = Executors.newFixedThreadPool(
                maxConcurrency,
                r -> {
                    Thread thread = new Thread(r);
                    thread.setName("chapter-worker-" + thread.getId());
                    thread.setDaemon(true);
                    return thread;
                }
        );
        
        log.info("章节调度器已初始化，最大并发数: {}", maxConcurrency);
    }

    /**
     * 执行所有章节学习任务
     *
     * @param chapterTasks 章节任务列表
     * @param worker 工作函数（学习单个章节的逻辑）
     */
    public void execute(List<ChapterTaskDTO> chapterTasks, ChapterWorker worker) {
        if (chapterTasks == null || chapterTasks.isEmpty()) {
            log.warn("没有章节需要学习");
            return;
        }

        log.info("═══════════════════════════════════════");
        log.info("🚀 开始并发学习{}个章节", chapterTasks.size());
        log.info("最大并发数: {}", maxConcurrency);
        log.info("═══════════════════════════════════════");

        progress.setRunning(true);
        progress.setStartTime(java.time.LocalDateTime.now());

        List<Future<?>> futures = new ArrayList<>();

        // 提交所有任务到线程池
        for (ChapterTaskDTO task : chapterTasks) {
            Future<?> future = executorService.submit(() -> {
                try {
                    activeThreads.incrementAndGet();
                    processChapter(task, worker);
                } catch (Exception e) {
                    log.error("章节处理异常: {}", task.getChapterName(), e);
                    task.markError(e.getMessage());
                } finally {
                    activeThreads.decrementAndGet();
                    progress.updateStatistics();
                }
            });
            futures.add(future);
        }

        // 等待所有任务完成
        for (Future<?> future : futures) {
            try {
                future.get(); // 阻塞等待
            } catch (InterruptedException e) {
                log.error("等待任务完成时被中断", e);
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                log.error("任务执行异常", e.getCause());
            }
        }

        // 处理重试队列
        processRetryQueue(worker);

        progress.setRunning(false);
        progress.setEndTime(java.time.LocalDateTime.now());
        progress.updateStatistics();

        // 输出最终报告
        log.info("\n{}", progress.generateReport());
    }

    /**
     * 处理单个章节
     *
     * @param task 章节任务
     * @param worker 工作函数
     */
    private void processChapter(ChapterTaskDTO task, ChapterWorker worker) {
        String chapterName = task.getChapterName() != null ? task.getChapterName() : "未知章节";
        
        log.info("📖 [线程-{}] 开始处理章节: {} [{}/{}]", 
                Thread.currentThread().getName(),
                chapterName,
                task.getIndex() + 1,
                progress.getTotalChapters());

        task.markRunning();
        progress.setCurrentChapterIndex(task.getIndex());

        try {
            // 调用工作函数学习章节
            worker.study(task);

            // 检查是否未开放
            if (task.isNotOpen()) {
                log.warn("⚠️  章节未开放: {}", chapterName);
                handleNotOpenChapter(task, worker);
            } else {
                // 检查是否成功
                if (task.getStatus() == ChapterTaskStatus.RUNNING) {
                    // 如果状态仍是RUNNING，说明成功了
                    task.markSuccess("学习完成");
                    log.info("✅ [线程-{}] 章节学习成功: {}", 
                            Thread.currentThread().getName(), chapterName);
                }
            }

        } catch (Exception e) {
            log.error("❌ [线程-{}] 章节学习失败: {} - {}", 
                    Thread.currentThread().getName(),
                    chapterName,
                    e.getMessage());
            task.markError(e.getMessage());
            
            // 加入重试队列
            if (task.canRetry()) {
                retryQueue.offer(task);
                log.info("🔄 章节已加入重试队列: {} (剩余重试次数: {})", 
                        chapterName, task.getMaxTries() - task.getTries());
            }
        }
    }

    /**
     * 处理未开放章节
     *
     * @param task 章节任务
     * @param worker 工作函数
     */
    private void handleNotOpenChapter(ChapterTaskDTO task, ChapterWorker worker) {
        String chapterName = task.getChapterName();
        
        if (task.canRetry()) {
            log.info("🔄 未开放章节将稍后重试: {} (第{}次尝试)", 
                    chapterName, task.getTries());
            retryQueue.offer(task);
        } else {
            log.warn("⚠️  未开放章节超过最大重试次数，标记为跳过: {}", chapterName);
            task.setStatus(ChapterTaskStatus.MAX_RETRY_EXCEEDED);
        }
    }

    /**
     * 处理重试队列
     *
     * @param worker 工作函数
     */
    private void processRetryQueue(ChapterWorker worker) {
        if (retryQueue.isEmpty()) {
            log.info("✅ 没有需要重试的章节");
            return;
        }

        log.info("═══════════════════════════════════════");
        log.info("🔄 开始处理重试队列，共{}个章节", retryQueue.size());
        log.info("═══════════════════════════════════════");

        List<ChapterTaskDTO> retryTasks = new ArrayList<>();
        retryQueue.drainTo(retryTasks);

        // 等待一段时间后重试
        try {
            log.info("⏳ 等待30秒后开始重试...");
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("等待重试时被中断", e);
            return;
        }

        // 重新提交重试任务
        for (ChapterTaskDTO task : retryTasks) {
            log.info("🔄 重试章节: {} (第{}次尝试)", 
                    task.getChapterName(), task.getTries());
            
            try {
                processChapter(task, worker);
            } catch (Exception e) {
                log.error("重试章节失败: {}", task.getChapterName(), e);
            }
        }
    }

    /**
     * 关闭调度器
     */
    public void shutdown() {
        log.info("正在关闭章节调度器...");
        executorService.shutdown();
        
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                log.warn("强制关闭线程池");
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("关闭线程池时被中断", e);
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        log.info("章节调度器已关闭");
    }

    /**
     * 获取活跃线程数
     *
     * @return 活跃线程数
     */
    public int getActiveThreadCount() {
        return activeThreads.get();
    }

    /**
     * 获取重试队列大小
     *
     * @return 重试队列大小
     */
    public int getRetryQueueSize() {
        return retryQueue.size();
    }

    /**
     * 章节工作函数接口
     */
    @FunctionalInterface
    public interface ChapterWorker {
        /**
         * 学习单个章节
         *
         * @param task 章节任务
         */
        void study(ChapterTaskDTO task);
    }
}
