package com.chaoxingweb.chaoxing.tiku;

import com.chaoxingweb.chaoxing.dto.QuestionDTO;
import com.chaoxingweb.chaoxing.dto.TikuConfigDTO;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 测验答题管理器
 * 
 * 职责：
 * - 管理答题流程
 * - 计算覆盖率
 * - 控制提交策略
 * - 整合题库查询和答案匹配
 *
 * @author 小克 🐕💎
 * @since 2026-04-09
 */
@Slf4j
public class WorkAnswerManager {

    private final TikuService tikuService;
    private final AnswerMatcher answerMatcher;
    private final TikuConfigDTO config;

    public WorkAnswerManager(TikuService tikuService, AnswerMatcher answerMatcher) {
        this.tikuService = tikuService;
        this.answerMatcher = answerMatcher;
        this.config = tikuService.getConfig();
    }

    /**
     * 处理所有题目，填充答案
     *
     * @param questions 题目列表
     * @return 找到的答案数量
     */
    public int processAllQuestions(List<QuestionDTO> questions) {
        if (questions == null || questions.isEmpty()) {
            log.warn("题目列表为空");
            return 0;
        }

        int foundAnswers = 0;
        int totalQuestions = questions.size();

        log.info("开始处理{}道题目...", totalQuestions);

        for (int i = 0; i < questions.size(); i++) {
            QuestionDTO question = questions.get(i);
            
            try {
                // 添加查询延迟
                if (config.getQueryDelay() > 0) {
                    Thread.sleep((long) (config.getQueryDelay() * 1000));
                }

                // 查询题库答案
                String answer = tikuService.query(question);
                
                String finalAnswer = "";
                String answerSource = "random";

                if (answer != null && !answer.isEmpty()) {
                    // 根据题型处理答案
                    finalAnswer = matchAnswerByType(answer, question);
                    
                    if (finalAnswer != null && !finalAnswer.isEmpty()) {
                        answerSource = "cover";
                        foundAnswers++;
                        log.info("✅ [{}] 成功获取答案: {}", i + 1, finalAnswer);
                    } else {
                        log.warn("⚠️ [{}] 找到答案但未能匹配，使用随机答案", i + 1);
                    }
                }

                // 如果未匹配到答案，使用随机策略
                if (finalAnswer == null || finalAnswer.isEmpty()) {
                    finalAnswer = generateRandomAnswer(question);
                    log.info("🎲 [{}] 随机生成答案: {}", i + 1, finalAnswer);
                }

                // 填充答案
                question.setFinalAnswer(finalAnswer);
                question.setAnswerSource(answerSource);
                question.setAnswered(true);
                
                // 更新答案字段映射
                if (question.getAnswerField() != null) {
                    question.getAnswerField().put("answer" + question.getId(), finalAnswer);
                }

                log.info("[{}/{}] {} -> 答案: {}, 来源: {}", 
                        i + 1, totalQuestions, 
                        truncateTitle(question.getTitle()), 
                        finalAnswer, 
                        answerSource);

            } catch (Exception e) {
                log.error("处理题目异常: {}", question.getTitle(), e);
                
                // 异常时使用随机答案
                String randomAnswer = generateRandomAnswer(question);
                question.setFinalAnswer(randomAnswer);
                question.setAnswerSource("random");
                question.setAnswered(true);
            }
        }

        // 计算覆盖率
        double coverRate = (double) foundAnswers / totalQuestions * 100;
        log.info("📊 章节检测题库覆盖率: {}/{} ({:.0f}%)", foundAnswers, totalQuestions, coverRate);

        return foundAnswers;
    }

    /**
     * 根据题型匹配答案
     *
     * @param answer 题库返回的答案
     * @param question 题目信息
     * @return 匹配后的答案
     */
    private String matchAnswerByType(String answer, QuestionDTO question) {
        return switch (question.getType()) {
            case SINGLE -> answerMatcher.matchSingleChoice(answer, question.getOptions());
            case MULTIPLE -> answerMatcher.matchMultipleChoice(answer, question.getOptions());
            case JUDGEMENT -> answerMatcher.convertJudgementAnswer(answer, tikuService);
            case COMPLETION -> answerMatcher.processCompletionAnswer(answer);
            case ESSAY -> answer; // 简答题直接使用
            default -> answer;
        };
    }

    /**
     * 生成随机答案
     *
     * @param question 题目信息
     * @return 随机答案
     */
    private String generateRandomAnswer(QuestionDTO question) {
        return switch (question.getType()) {
            case SINGLE -> answerMatcher.randomSingleChoice(question.getOptions());
            case MULTIPLE -> answerMatcher.randomMultipleChoice(question.getOptions());
            case JUDGEMENT -> answerMatcher.randomJudgement();
            case COMPLETION -> "未知";
            case ESSAY -> "略";
            default -> "A";
        };
    }

    /**
     * 获取提交参数（pyFlag）
     *
     * @param foundAnswers 找到的答案数量
     * @param totalQuestions 总题目数量
     * @return pyFlag值（空字符串=提交，"1"=保存）
     */
    public String getPyFlag(int foundAnswers, int totalQuestions) {
        if (totalQuestions == 0) {
            return "1";
        }

        double coverRate = (double) foundAnswers / totalQuestions;
        String pyFlag = tikuService.getPyFlag(coverRate);

        if ("1".equals(pyFlag)) {
            log.info("⏸️  题库覆盖率{:.0f}%低于阈值{:.0f}%，仅保存不提交", 
                    coverRate * 100, config.getCoverRate() * 100);
        } else {
            log.info("✅ 题库覆盖率{:.0f}%达到阈值，将提交答案", coverRate * 100);
        }

        return pyFlag;
    }

    /**
     * 构建提交表单数据
     *
     * @param questions 题目列表
     * @param formData 基础表单数据
     * @param pyFlag 提交标志
     * @return 完整的提交表单数据
     */
    public java.util.Map<String, String> buildSubmitFormData(
            List<QuestionDTO> questions,
            java.util.Map<String, String> formData,
            String pyFlag) {

        // 复制基础表单数据
        java.util.Map<String, String> submitData = new java.util.HashMap<>(formData);

        // 添加pyFlag
        submitData.put("pyFlag", pyFlag);

        // 添加每道题的答案
        for (QuestionDTO question : questions) {
            String questionId = question.getId();
            String answer = question.getFinalAnswer();
            String answerType = question.getAnswerField() != null 
                    ? question.getAnswerField().getOrDefault("answertype" + questionId, "0")
                    : "0";

            // 如果是保存模式且答案是随机的，则不填充
            if ("1".equals(pyFlag) && "random".equals(question.getAnswerSource())) {
                submitData.put("answer" + questionId, "");
            } else {
                submitData.put("answer" + questionId, answer != null ? answer : "");
            }

            submitData.put("answertype" + questionId, answerType);
        }

        log.debug("构建提交表单完成，共{}个字段", submitData.size());
        return submitData;
    }

    /**
     * 截断标题用于日志显示
     *
     * @param title 标题
     * @return 截断后的标题
     */
    private String truncateTitle(String title) {
        if (title == null) {
            return "";
        }
        return title.length() > 50 ? title.substring(0, 50) + "..." : title;
    }
}
