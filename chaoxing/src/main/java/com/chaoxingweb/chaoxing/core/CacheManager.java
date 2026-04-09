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
        cacheMap.put(key, new CacheItem<>(value, ttlMinutes));
        log.debug("设置缓存: key={}, ttl={}min", key, ttlMinutes);
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
