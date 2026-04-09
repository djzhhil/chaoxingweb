package com.chaoxingweb.chaoxing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 测验题目列表DTO
 *
 * @author 小克 🐕💎
 * @since 2026-04-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkQuestionsDTO {

    /**
     * 表单数据（非答案字段）
     */
    private Map<String, String> formData;

    /**
     * 题目列表
     */
    private List<QuestionDTO> questions;

    /**
     * 题目ID列表（逗号分隔）
     */
    private String answerwqbid;

    /**
     * 工作ID
     */
    private String workId;

    /**
     * 作业ID
     */
    private String jobid;

    /**
     * 知识点ID
     */
    private String knowledgeid;

    /**
     * 班级ID
     */
    private String clazzId;

    /**
     * 课程ID
     */
    private String courseId;

    /**
     * 原始HTML内容（用于调试）
     */
    private String rawHtml;
}
