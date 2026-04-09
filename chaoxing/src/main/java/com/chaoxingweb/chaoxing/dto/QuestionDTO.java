package com.chaoxingweb.chaoxing.dto;

import com.chaoxingweb.chaoxing.enums.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 题目信息DTO
 *
 * @author 小克 🐕💎
 * @since 2026-04-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDTO {

    /**
     * 题目ID
     */
    private String id;

    /**
     * 题目标题
     */
    private String title;

    /**
     * 题目类型
     */
    private QuestionType type;

    /**
     * 选项列表（单选/多选题）
     */
    private List<String> options;

    /**
     * 答案字段映射
     * key: answer{questionId}, answertype{questionId}
     * value: 答案内容, 答案类型
     */
    private Map<String, String> answerField;

    /**
     * 答案来源：cover（题库覆盖）/ random（随机）
     */
    private String answerSource;

    /**
     * 查询到的答案
     */
    private String queriedAnswer;

    /**
     * 最终填写的答案
     */
    private String finalAnswer;

    /**
     * 是否已回答
     */
    private boolean answered;

    /**
     * 题目分数
     */
    private Double score;

    /**
     * 原始HTML内容（用于调试）
     */
    private String rawHtml;
}
