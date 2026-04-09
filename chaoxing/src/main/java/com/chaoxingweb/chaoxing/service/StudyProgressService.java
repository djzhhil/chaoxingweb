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
}
