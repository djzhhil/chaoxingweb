package com.chaoxingweb.chaoxing.core.impl;

import com.chaoxingweb.chaoxing.core.RateLimiter;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 速率限制器实现
 *
 * @author 小克 🐕💎
 * @since 2026-03-30
 */
@Component
public class RateLimiterImpl implements RateLimiter {

    private double callInterval; // 调用间隔（秒）
    private long lastCallTime;  // 上次调用时间（毫秒）
    private final Lock lock;     // 锁
    private final Random random; // 随机数生成器

    public RateLimiterImpl() {
        this.callInterval = 0.5; // 默认 0.5 秒
        this.lastCallTime = System.currentTimeMillis();
        this.lock = new ReentrantLock();
        this.random = new Random();
    }

    @Override
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

    @Override
    public void limitRateWithRandom(double min, double max) {
        lock.lock();
        try {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - lastCallTime;

            // 生成随机间隔
            double randomInterval = min + (max - min) * random.nextDouble();
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

    @Override
    public double getCurrentRate() {
        return 1.0 / callInterval;
    }

    @Override
    public void setRate(double rate) {
        if (rate <= 0) {
            throw new IllegalArgumentException("Rate must be positive");
        }
        this.callInterval = 1.0 / rate;
    }

    @Override
    public long getLastCallTime() {
        return lastCallTime;
    }
}
