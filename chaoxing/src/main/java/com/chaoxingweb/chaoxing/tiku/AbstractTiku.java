package com.chaoxingweb.chaoxing.tiku;

import com.chaoxingweb.chaoxing.dto.QuestionDTO;
import com.chaoxingweb.chaoxing.dto.TikuConfigDTO;
import com.chaoxingweb.chaoxing.enums.QuestionType;
import com.chaoxingweb.chaoxing.tiku.cache.AnswerCache;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

/**
 * 题库抽象基类
 * 
 * 提供通用功能：
 * - 缓存管理
 * - 答案验证
 * - 配置管理
 * - 判断题处理
 *
 * @author 小克 🐕💎
 * @since 2026-04-09
 */
@Slf4j
public abstract class AbstractTiku implements TikuService {

    protected TikuConfigDTO config;
    protected AnswerCache cache;
    protected String name;

    public AbstractTiku() {
        this.config = TikuConfigDTO.builder().build();
    }

    @Override
    public void init() {
        log.info("初始化题库: {}", getName());
        
        // 验证配置
        if (config != null && !config.isValid()) {
            log.warn("题库配置无效，将禁用题库功能");
            config.setEnabled(false);
        }
        
        // 调用子类的初始化逻辑
        initTiku();
    }

    /**
     * 子类实现的初始化逻辑
     */
    protected abstract void initTiku();

    @Override
    public String query(QuestionDTO question) {
        if (!isEnabled()) {
            log.debug("题库未启用，跳过查询");
            return null;
        }

        if (question == null || question.getTitle() == null) {
            log.warn("题目信息为空，无法查询");
            return null;
        }

        // 预处理题目标题
        String title = preprocessTitle(question.getTitle());
        log.debug("原始标题: {}", question.getTitle());
        log.debug("处理后标题: {}", title);

        // 先查缓存
        String cachedAnswer = cache.getAnswer(title);
        if (cachedAnswer != null) {
            log.info("从缓存中获取答案: {} -> {}", title, cachedAnswer);
            return cachedAnswer.trim();
        }

        // 缓存未命中，查询题库
        try {
            String answer = doQuery(question);
            
            if (answer != null && !answer.isEmpty()) {
                answer = answer.trim();
                
                // 验证答案类型是否匹配
                if (validateAnswer(answer, question.getType())) {
                    log.info("从{}获取答案: {} -> {}", getName(), title, answer);
                    
                    // 添加到缓存
                    cache.addAnswer(title, answer);
                    
                    return answer;
                } else {
                    log.warn("答案类型与题目类型不符，已舍弃: {}", answer);
                    return null;
                }
            }
            
            log.error("从{}获取答案失败: {}", getName(), title);
            return null;
            
        } catch (Exception e) {
            log.error("查询题库异常: {}", title, e);
            return null;
        }
    }

    /**
     * 子类实现的具体查询逻辑
     *
     * @param question 题目信息
     * @return 答案字符串
     */
    protected abstract String doQuery(QuestionDTO question);

    @Override
    public boolean judgementSelect(String answer) {
        if (!isEnabled()) {
            return false;
        }

        if (answer == null) {
            return false;
        }

        answer = answer.trim();

        // 检查是否在正确选项列表中
        if (config.getTrueList().contains(answer)) {
            return true;
        }
        
        // 检查是否在错误选项列表中
        if (config.getFalseList().contains(answer)) {
            return false;
        }

        // 无法判断，随机选择
        log.warn("无法判断答案 '{}' 对应的是正确还是错误，本次将随机选择", answer);
        return Math.random() > 0.5;
    }

    @Override
    public String getPyFlag(double currentCoverRate) {
        if (config == null) {
            return "1"; // 默认不提交
        }
        return config.getPyFlag(currentCoverRate);
    }

    @Override
    public TikuConfigDTO getConfig() {
        return config;
    }

    @Override
    public void setConfig(TikuConfigDTO config) {
        this.config = config;
    }

    @Override
    public boolean isEnabled() {
        return config != null && config.isEnabled();
    }

    /**
     * 获取题库名称
     *
     * @return 题库名称
     */
    public String getName() {
        return name != null ? name : "Unknown";
    }

    /**
     * 预处理题目标题
     * 去除序号、分数等无关字段
     *
     * @param title 原始标题
     * @return 处理后的标题
     */
    protected String preprocessTitle(String title) {
        if (title == null) {
            return "";
        }

        // 去除开头的数字序号
        title = title.replaceAll("^\\d+", "");
        
        // 去除末尾的分数标注（如"（5.0分）"）
        title = title.replaceAll("（\\d+\\.\\d+分）$", "");
        
        return title.trim();
    }

    /**
     * 验证答案类型是否与题目类型匹配
     *
     * @param answer 答案
     * @param questionType 题目类型
     * @return 是否匹配
     */
    protected boolean validateAnswer(String answer, QuestionType questionType) {
        if (answer == null || answer.isEmpty()) {
            return false;
        }

        return switch (questionType) {
            case SINGLE -> {
                // 单选题答案应该是单个字母（A/B/C/D）
                yield Pattern.matches("^[A-D]$", answer.toUpperCase());
            }
            case MULTIPLE -> {
                // 多选题答案应该是多个字母组合（ABC/ABD等）
                yield Pattern.matches("^[A-D]{2,4}$", answer.toUpperCase());
            }
            case JUDGEMENT -> {
                // 判断题答案应该是true/false或对/错等
                yield answer.equalsIgnoreCase("true") || 
                      answer.equalsIgnoreCase("false") ||
                      config.getTrueList().contains(answer) ||
                      config.getFalseList().contains(answer);
            }
            case COMPLETION -> true; // 填空题不做验证
            case ESSAY -> true; // 简答题不做验证
            default -> true;
        };
    }

    /**
     * 清理答案中的前缀字母（如"A. "、"B. "）
     *
     * @param answer 原始答案
     * @return 清理后的答案
     */
    protected String cleanAnswerPrefix(String answer) {
        if (answer == null) {
            return null;
        }
        
        // 去除开头的字母编号（如"A."、"B,"等）
        return answer.replaceAll("^[A-Za-z][.,!?;:，。！？；：]", "").trim();
    }
}
