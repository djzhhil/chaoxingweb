package com.chaoxingweb.chaoxing.scheduler;

import com.chaoxingweb.chaoxing.core.CacheManager;
import com.chaoxingweb.chaoxing.core.MultiTenantSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 会话清理定时任务
 *
 * @author 小克 🐕💎
 * @since 2026-04-09
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SessionCleanupScheduler {

    private final MultiTenantSessionManager sessionManager;
    private final CacheManager cacheManager;

    /**
     * 每10分钟清理一次过期会话
     */
    @Scheduled(fixedRate = 600000) // 10分钟 = 600000毫秒
    public void cleanupExpiredSessions() {
        log.debug("开始执行会话清理任务");
        try {
            int cleanedCount = sessionManager.cleanExpiredSessions();
            if (cleanedCount > 0) {
                log.info("会话清理完成，清理{}个过期会话，当前活跃会话数: {}", 
                        cleanedCount, sessionManager.getActiveSessionCount());
            }
        } catch (Exception e) {
            log.error("会话清理任务执行失败", e);
        }
    }

    /**
     * 每30分钟清理一次过期缓存
     */
    @Scheduled(fixedRate = 1800000) // 30分钟 = 1800000毫秒
    public void cleanupExpiredCache() {
        log.debug("开始执行缓存清理任务");
        try {
            int cleanedCount = cacheManager.cleanExpired();
            if (cleanedCount > 0) {
                log.info("缓存清理完成，清理{}条过期缓存，当前缓存数: {}", 
                        cleanedCount, cacheManager.size());
            }
        } catch (Exception e) {
            log.error("缓存清理任务执行失败", e);
        }
    }
}
