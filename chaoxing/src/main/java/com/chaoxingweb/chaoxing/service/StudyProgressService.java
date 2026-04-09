package com.chaoxingweb.chaoxing.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 学习进度SSE服务
 * 管理SSE连接并推送进度
 *
 * @author 小克 🐕💎
 * @since 2026-04-08
 */
@Slf4j
@Service
public class StudyProgressService {

    // 存储 jobId -> SseEmitter 的映射
    private final Map<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();

    /**
     * 创建SSE连接
     *
     * @param jobId 任务ID
     * @return SseEmitter
     */
    public SseEmitter createConnection(String jobId) {
        // 超时时间30分钟
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
        
        emitter.onCompletion(() -> {
            log.debug("SSE连接完成: jobId={}", jobId);
            emitterMap.remove(jobId);
        });
        
        emitter.onTimeout(() -> {
            log.warn("SSE连接超时: jobId={}", jobId);
            emitterMap.remove(jobId);
        });
        
        emitter.onError((ex) -> {
            log.error("SSE连接错误: jobId={}", jobId, ex);
            emitterMap.remove(jobId);
        });
        
        emitterMap.put(jobId, emitter);
        log.info("创建SSE连接: jobId={}", jobId);
        
        return emitter;
    }

    /**
     * 推送进度
     *
     * @param jobId   任务ID
     * @param progress 进度信息
     */
    public void pushProgress(String jobId, Object progress) {
        SseEmitter emitter = emitterMap.get(jobId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("progress")
                        .data(progress));
                log.debug("推送进度成功: jobId={}, progress={}", jobId, progress);
            } catch (IOException e) {
                log.error("推送进度失败: jobId={}", jobId, e);
                emitterMap.remove(jobId);
            }
        } else {
            log.debug("未找到SSE连接: jobId={}", jobId);
        }
    }

    /**
     * 关闭连接
     *
     * @param jobId 任务ID
     */
    public void closeConnection(String jobId) {
        SseEmitter emitter = emitterMap.remove(jobId);
        if (emitter != null) {
            try {
                emitter.complete();
                log.info("关闭SSE连接: jobId={}", jobId);
            } catch (Exception e) {
                log.error("关闭SSE连接异常: jobId={}", jobId, e);
            }
        }
    }

    /**
     * 创建课程学习SSE连接
     *
     * @param courseId 课程ID
     * @return SseEmitter
     */
    public SseEmitter createCourseConnection(String courseId) {
        // 超时时间60分钟
        SseEmitter emitter = new SseEmitter(60 * 60 * 1000L);
        
        String connectionId = "course-" + courseId;
        
        emitter.onCompletion(() -> {
            log.debug("课程SSE连接完成: {}", connectionId);
            emitterMap.remove(connectionId);
        });
        
        emitter.onTimeout(() -> {
            log.warn("课程SSE连接超时: {}", connectionId);
            emitterMap.remove(connectionId);
        });
        
        emitter.onError((ex) -> {
            log.error("课程SSE连接错误: {}", connectionId, ex);
            emitterMap.remove(connectionId);
        });
        
        emitterMap.put(connectionId, emitter);
        log.info("创建课程SSE连接: {}", connectionId);
        
        return emitter;
    }

    /**
     * 推送课程学习进度
     *
     * @param courseId 课程ID
     * @param progress 进度信息（LearningProgressDTO）
     */
    public void pushCourseProgress(String courseId, Object progress) {
        String connectionId = "course-" + courseId;
        SseEmitter emitter = emitterMap.get(connectionId);
        
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("course-progress")
                        .data(progress));
                log.trace("推送课程进度成功: {}", connectionId);
            } catch (IOException e) {
                log.error("推送课程进度失败: {}", connectionId, e);
                emitterMap.remove(connectionId);
            }
        }
    }

    /**
     * 推送章节学习进度
     *
     * @param courseId 课程ID
     * @param chapterIndex 章节索引
     * @param chapterName 章节名称
     * @param status 状态
     * @param message 消息
     */
    public void pushChapterProgress(String courseId, int chapterIndex, 
                                    String chapterName, String status, String message) {
        String connectionId = "course-" + courseId;
        SseEmitter emitter = emitterMap.get(connectionId);
        
        if (emitter != null) {
            try {
                Map<String, Object> data = Map.of(
                        "type", "chapter",
                        "chapterIndex", chapterIndex,
                        "chapterName", chapterName != null ? chapterName : "未知章节",
                        "status", status,
                        "message", message != null ? message : "",
                        "timestamp", System.currentTimeMillis()
                );
                
                emitter.send(SseEmitter.event()
                        .name("chapter-progress")
                        .data(data));
                
                log.debug("推送章节进度: [{}] {} - {}", chapterIndex + 1, chapterName, status);
            } catch (IOException e) {
                log.error("推送章节进度失败", e);
                emitterMap.remove(connectionId);
            }
        }
    }

    /**
     * 推送日志消息
     *
     * @param courseId 课程ID
     * @param level 日志级别（INFO/WARN/ERROR）
     * @param message 日志消息
     */
    public void pushLogMessage(String courseId, String level, String message) {
        String connectionId = "course-" + courseId;
        SseEmitter emitter = emitterMap.get(connectionId);
        
        if (emitter != null) {
            try {
                Map<String, Object> data = Map.of(
                        "type", "log",
                        "level", level,
                        "message", message,
                        "timestamp", System.currentTimeMillis()
                );
                
                emitter.send(SseEmitter.event()
                        .name("log-message")
                        .data(data));
            } catch (IOException e) {
                log.error("推送日志消息失败", e);
                emitterMap.remove(connectionId);
            }
        }
    }

    /**
     * 关闭课程连接
     *
     * @param courseId 课程ID
     */
    public void closeCourseConnection(String courseId) {
        String connectionId = "course-" + courseId;
        SseEmitter emitter = emitterMap.remove(connectionId);
        
        if (emitter != null) {
            try {
                emitter.complete();
                log.info("关闭课程SSE连接: {}", connectionId);
            } catch (Exception e) {
                log.error("关闭课程SSE连接异常: {}", connectionId, e);
            }
        }
    }
}
