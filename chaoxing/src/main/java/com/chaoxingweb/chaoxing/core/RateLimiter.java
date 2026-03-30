package com.chaoxingweb.chaoxing.core;

/**
 * 速率限制器接口
 *
 * @author 小克 🐕💎
 * @since 2026-03-30
 */
public interface RateLimiter {

    /**
     * 限制速率
     */
    void limitRate();

    /**
     * 限制速率（随机时间）
     *
     * @param min 最小时间（秒）
     * @param max 最大时间（秒）
     */
    void limitRateWithRandom(double min, double max);

    /**
     * 获取当前速率
     *
     * @return 当前速率（次/秒）
     */
    double getCurrentRate();

    /**
     * 设置速率
     *
     * @param rate 速率（次/秒）
     */
    void setRate(double rate);

    /**
     * 获取上次调用时间
     *
     * @return 上次调用时间（毫秒）
     */
    long getLastCallTime();
}
