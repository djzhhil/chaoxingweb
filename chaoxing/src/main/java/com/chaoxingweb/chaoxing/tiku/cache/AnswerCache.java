package com.chaoxingweb.chaoxing.tiku.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 答案缓存管理器
 * 
 * 功能：
 * - 基于JSON文件的持久化缓存
 * - 线程安全的读写操作
 * - 自动加载和保存
 *
 * @author 小克 🐕💎
 * @since 2026-04-09
 */
@Slf4j
@Component
public class AnswerCache {

    private static final String CACHE_FILE = "cache.json";
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, String> cache = new ConcurrentHashMap<>();
    private File cacheFile;

    @PostConstruct
    public void init() {
        // 初始化缓存文件路径
        cacheFile = new File(CACHE_FILE);
        
        // 如果文件不存在，创建空文件
        if (!cacheFile.exists()) {
            try {
                cacheFile.createNewFile();
                saveCache();
                log.info("创建新的缓存文件: {}", cacheFile.getAbsolutePath());
            } catch (IOException e) {
                log.error("创建缓存文件失败", e);
            }
        } else {
            // 加载现有缓存
            loadCache();
            log.info("加载缓存完成，共{}条记录", cache.size());
        }
    }

    /**
     * 从缓存中获取答案
     *
     * @param question 题目标题
     * @return 答案，null表示未找到
     */
    public String getAnswer(String question) {
        if (question == null || question.isEmpty()) {
            return null;
        }
        
        String answer = cache.get(question);
        if (answer != null) {
            log.debug("从缓存中获取答案: {} -> {}", 
                    question.length() > 30 ? question.substring(0, 30) + "..." : question, 
                    answer);
        }
        
        return answer;
    }

    /**
     * 添加答案到缓存
     *
     * @param question 题目标题
     * @param answer 答案
     */
    public void addAnswer(String question, String answer) {
        if (question == null || question.isEmpty() || answer == null) {
            return;
        }
        
        cache.put(question, answer);
        saveCache();
        
        log.debug("添加答案到缓存: {} -> {}", 
                question.length() > 30 ? question.substring(0, 30) + "..." : question, 
                answer);
    }

    /**
     * 从文件加载缓存
     */
    private void loadCache() {
        try {
            if (cacheFile.exists() && cacheFile.length() > 0) {
                Map<String, String> loadedCache = objectMapper.readValue(
                        cacheFile, 
                        new TypeReference<Map<String, String>>() {}
                );
                cache.clear();
                cache.putAll(loadedCache);
                log.info("成功加载缓存文件: {} 条记录", cache.size());
            }
        } catch (IOException e) {
            log.error("加载缓存文件失败，将使用空缓存", e);
            // 备份损坏的文件
            backupCorruptedCache();
        }
    }

    /**
     * 保存缓存到文件
     */
    private synchronized void saveCache() {
        try {
            // 写入临时文件后原子替换，减少并发写入时的损坏风险
            File tempFile = new File(cacheFile.getAbsolutePath() + ".tmp");
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(tempFile, cache);
            
            // 原子替换
            if (cacheFile.exists()) {
                cacheFile.delete();
            }
            tempFile.renameTo(cacheFile);
            
            log.trace("缓存已保存到文件: {} 条记录", cache.size());
        } catch (IOException e) {
            log.error("保存缓存文件失败", e);
        }
    }

    /**
     * 备份损坏的缓存文件
     */
    private void backupCorruptedCache() {
        try {
            String backupName = cacheFile.getName() + ".bak." + System.currentTimeMillis();
            File backupFile = new File(cacheFile.getParent(), backupName);
            
            if (cacheFile.renameTo(backupFile)) {
                log.warn("缓存文件已损坏，已备份为: {}", backupFile.getAbsolutePath());
            }
        } catch (Exception e) {
            log.error("备份损坏缓存失败", e);
        }
    }

    /**
     * 获取缓存大小
     *
     * @return 缓存记录数
     */
    public int size() {
        return cache.size();
    }

    /**
     * 清空缓存
     */
    public void clear() {
        cache.clear();
        saveCache();
        log.info("缓存已清空");
    }
}
