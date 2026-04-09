package com.chaoxingweb.chaoxing.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通用缓存管理器 - 支持过期时间
 *
 * @author 小克 🐕💎
 * @since 2026-04-09
 */
@Slf4j
@Component
public class CacheManager {

    /**
     * 默认最大缓存数量
     */
    private static final int DEFAULT_MAX_SIZE = 1000;

    /**
     * 最大缓存数量
     */
    private final int maxSize;

    /**
     * 缓存项
     */
    private static class CacheItem<T> {
        private final T data;
        private final LocalDateTime expireTime;

        public CacheItem(T data, long ttlMinutes) {
            this.data = data;
            this.expireTime = LocalDateTime.now().plusMinutes(ttlMinutes);
        }

        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expireTime);
        }

        public T getData() {
            return data;
        }
    }

    /**
     * 缓存存储：key -> CacheItem
     */
    private final Map<String, CacheItem<?>> cacheMap = new ConcurrentHashMap<>();

    public CacheManager() {
        this(DEFAULT_MAX_SIZE);
    }

    public CacheManager(int maxSize) {
        this.maxSize = maxSize;
        log.info("缓存管理器已初始化，最大容量: {}", maxSize);
    }

    /**
     * 获取缓存
     *
     * @param key 缓存键
     * @param clazz 数据类型
     * @return 缓存数据，不存在或已过期返回 null
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        CacheItem<?> item = cacheMap.get(key);
        
        if (item == null) {
            log.debug("缓存未命中: key={}", key);
            return null;
        }
        
        if (item.isExpired()) {
            cacheMap.remove(key);
            log.debug("缓存已过期，已移除: key={}", key);
            return null;
        }
        
        log.debug("缓存命中: key={}", key);
        return (T) item.getData();
    }

    /**
     * 设置缓存
     *
     * @param key 缓存键
     * @param value 缓存值
     * @param ttlMinutes 过期时间（分钟）
     */
    public void put(String key, Object value, long ttlMinutes) {
        // 检查是否超过最大容量
        if (cacheMap.size() >= maxSize && !cacheMap.containsKey(key)) {
            log.warn("缓存已满（{}条），清理最旧的10%缓存", cacheMap.size());
            evictOldest(10); // 清理最旧的10%
        }
        
        cacheMap.put(key, new CacheItem<>(value, ttlMinutes));
        log.debug("设置缓存: key={}, ttl={}min", key, ttlMinutes);
    }

    /**
     * 清理最旧的缓存（按过期时间排序）
     *
     * @param percentage 清理百分比（0-100）
     */
    private void evictOldest(int percentage) {
        if (cacheMap.isEmpty()) {
            return;
        }

        int countToEvict = Math.max(1, cacheMap.size() * percentage / 100);
        
        // 找出最早过期的缓存
        cacheMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue((a, b) -> a.expireTime.compareTo(b.expireTime)))
                .limit(countToEvict)
                .forEach(entry -> {
                    cacheMap.remove(entry.getKey());
                    log.debug("清理旧缓存: key={}", entry.getKey());
                });
        
        log.info("已清理{}条旧缓存，当前缓存数: {}", countToEvict, cacheMap.size());
    }

    /**
     * 删除缓存
     *
     * @param key 缓存键
     */
    public void remove(String key) {
        cacheMap.remove(key);
        log.debug("删除缓存: key={}", key);
    }

    /**
     * 清空所有缓存
     */
    public void clear() {
        int size = cacheMap.size();
        cacheMap.clear();
        log.info("清空所有缓存，共{}条记录", size);
    }

    /**
     * 清理所有过期缓存
     *
     * @return 清理的数量
     */
    public int cleanExpired() {
        int count = 0;
        for (Map.Entry<String, CacheItem<?>> entry : cacheMap.entrySet()) {
            if (entry.getValue().isExpired()) {
                cacheMap.remove(entry.getKey());
                count++;
            }
        }
        
        if (count > 0) {
            log.debug("清理过期缓存: {}条", count);
        }
        
        return count;
    }

    /**
     * 获取缓存大小
     *
     * @return 缓存数量
     */
    public int size() {
        return cacheMap.size();
    }
}
