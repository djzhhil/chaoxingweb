package com.chaoxingweb.chaoxing.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 多租户速率限制器 - 支持多用户并发
 * 
 * 每个用户独立的限流器，互不影响
 *
 * @author 小克 🐕💎
 * @since 2026-04-09
 */
@Slf4j
@Component
public class MultiTenantRateLimiter {

    /**
     * 用户ID -> 限流器的映射
     */
    private final Map<String, UserRateLimiter> rateLimiterMap = new ConcurrentHashMap<>();

    /**
     * 默认调用间隔（秒）
     */
    private static final double DEFAULT_INTERVAL = 0.5;

    /**
     * 获取或创建用户的限流器
     *
     * @param userId 用户标识
     * @return 用户限流器
     */
    public UserRateLimiter getOrCreateRateLimiter(String userId) {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        return rateLimiterMap.computeIfAbsent(userId, key -> {
            log.debug("为用户创建新的限流器: userId={}", userId);
            return new UserRateLimiter(DEFAULT_INTERVAL);
        });
    }

    /**
     * 限流（使用默认间隔）
     *
     * @param userId 用户标识
     */
    public void limitRate(String userId) {
        getOrCreateRateLimiter(userId).limitRate();
    }

    /**
     * 限流（使用随机间隔）
     *
     * @param userId 用户标识
     * @param min 最小间隔（秒）
     * @param max 最大间隔（秒）
     */
    public void limitRateWithRandom(String userId, double min, double max) {
        getOrCreateRateLimiter(userId).limitRateWithRandom(min, max);
    }

    /**
     * 设置用户的请求速率
     *
     * @param userId 用户标识
     * @param rate 速率（次/秒）
     */
    public void setRate(String userId, double rate) {
        getOrCreateRateLimiter(userId).setRate(rate);
    }

    /**
     * 获取用户的当前速率
     *
     * @param userId 用户标识
     * @return 速率（次/秒）
     */
    public double getCurrentRate(String userId) {
        return getOrCreateRateLimiter(userId).getCurrentRate();
    }

    /**
     * 清理指定用户的限流器
     *
     * @param userId 用户标识
     */
    public void removeRateLimiter(String userId) {
        rateLimiterMap.remove(userId);
        log.debug("已移除用户限流器: userId={}", userId);
    }

    /**
     * 清理所有限流器
     */
    public void clear() {
        int size = rateLimiterMap.size();
        rateLimiterMap.clear();
        log.info("已清理所有限流器，共{}个", size);
    }

    /**
     * 获取活跃限流器数量
     *
     * @return 数量
     */
    public int getActiveCount() {
        return rateLimiterMap.size();
    }

    /**
     * 用户级别的限流器
     */
    public static class UserRateLimiter {
        private double callInterval; // 调用间隔（秒）
        private volatile long lastCallTime;  // 上次调用时间（毫秒）
        private final Lock lock;     // 锁

        public UserRateLimiter(double callInterval) {
            this.callInterval = callInterval;
            this.lastCallTime = System.currentTimeMillis();
            this.lock = new ReentrantLock();
        }

        /**
         * 限流（使用默认间隔）
         */
        public void limitRate() {
            lock.lock();
            try {
                long currentTime = System.currentTimeMillis();
                long elapsedTime = currentTime - lastCallTime;
                long requiredInterval = (long) (callInterval * 1000);

                if (elapsedTime < requiredInterval) {
                    long sleepTime = requiredInterval - elapsedTime;
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

                lastCallTime = System.currentTimeMillis();
            } finally {
                lock.unlock();
            }
        }

        /**
         * 限流（使用随机间隔）
         *
         * @param min 最小间隔（秒）
         * @param max 最大间隔（秒）
         */
        public void limitRateWithRandom(double min, double max) {
            lock.lock();
            try {
                long currentTime = System.currentTimeMillis();
                long elapsedTime = currentTime - lastCallTime;

                // 生成随机间隔（使用 ThreadLocalRandom 提高性能）
                double randomInterval = min + (max - min) * ThreadLocalRandom.current().nextDouble();
                long requiredInterval = (long) (randomInterval * 1000);

                if (elapsedTime < requiredInterval) {
                    long sleepTime = requiredInterval - elapsedTime;
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

                lastCallTime = System.currentTimeMillis();
            } finally {
                lock.unlock();
            }
        }

        /**
         * 获取当前速率
         *
         * @return 速率（次/秒）
         */
        public double getCurrentRate() {
            return 1.0 / callInterval;
        }

        /**
         * 设置速率
         *
         * @param rate 速率（次/秒）
         */
        public void setRate(double rate) {
            if (rate <= 0) {
                throw new IllegalArgumentException("Rate must be positive");
            }
            this.callInterval = 1.0 / rate;
        }

        /**
         * 获取上次调用时间
         *
         * @return 时间戳（毫秒）
         */
        public long getLastCallTime() {
            return lastCallTime;
        }
    }
}
