package com.chaoxingweb.chaoxing.tiku;

import com.chaoxingweb.chaoxing.dto.QuestionDTO;
import com.chaoxingweb.chaoxing.enums.QuestionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 答案匹配器
 * 
 * 职责：
 * - 单选题答案匹配
 * - 多选题答案匹配和排序
 * - 判断题答案转换
 * - 填空题答案拼接
 * - 随机答题策略
 * - 答案清洗和验证
 *
 * @author 小克 🐕💎
 * @since 2026-04-09
 */
@Slf4j
@Component
public class AnswerMatcher {

    /**
     * 匹配单选题答案
     * 将题库返回的答案与选项进行匹配，返回选项字母（A/B/C/D）
     *
     * @param answer 题库返回的答案
     * @param options 选项列表
     * @return 匹配的选项字母，未匹配返回null
     */
    public String matchSingleChoice(String answer, List<String> options) {
        if (answer == null || answer.isEmpty() || options == null || options.isEmpty()) {
            return null;
        }

        // 清理答案
        String cleanedAnswer = cleanAnswer(answer);

        // 遍历选项，查找子序列匹配
        for (String option : options) {
            if (isSubsequence(cleanedAnswer, option)) {
                String letter = extractOptionLetter(option);
                log.debug("单选题答案匹配: {} -> {}", answer, letter);
                return letter;
            }
        }

        log.warn("单选题答案未匹配: {}", answer);
        return null;
    }

    /**
     * 匹配多选题答案
     * 将题库返回的多个答案与选项匹配，返回排序后的选项字母组合（如ABC）
     *
     * @param answer 题库返回的答案（可能是多个答案的组合）
     * @param options 选项列表
     * @return 排序后的选项字母组合，未匹配返回null
     */
    public String matchMultipleChoice(String answer, List<String> options) {
        if (answer == null || answer.isEmpty() || options == null || options.isEmpty()) {
            return null;
        }

        // 分割多个答案
        List<String> answerList = splitMultipleAnswers(answer);
        Set<Character> matchedLetters = new TreeSet<>(); // 使用TreeSet自动排序

        // 对每个答案进行匹配
        for (String ans : answerList) {
            String cleanedAns = cleanAnswer(ans);
            
            for (String option : options) {
                if (isSubsequence(cleanedAns, option)) {
                    char letter = option.charAt(0); // 假设选项以字母开头
                    if (Character.isLetter(letter)) {
                        matchedLetters.add(Character.toUpperCase(letter));
                    }
                    break;
                }
            }
        }

        if (matchedLetters.isEmpty()) {
            log.warn("多选题答案未匹配: {}", answer);
            return null;
        }

        // 转换为字符串
        StringBuilder result = new StringBuilder();
        for (char c : matchedLetters) {
            result.append(c);
        }

        log.debug("多选题答案匹配: {} -> {}", answer, result);
        return result.toString();
    }

    /**
     * 转换判断题答案
     * 将题库返回的答案转换为true/false
     *
     * @param answer 题库返回的答案
     * @param tikuService 题库服务（用于判断选择）
     * @return "true" 或 "false"
     */
    public String convertJudgementAnswer(String answer, TikuService tikuService) {
        if (answer == null || answer.isEmpty()) {
            return randomJudgement();
        }

        boolean isTrue = tikuService.judgementSelect(answer);
        return isTrue ? "true" : "false";
    }

    /**
     * 处理填空题答案
     * 将多个填空答案拼接
     *
     * @param answer 题库返回的答案
     * @return 拼接后的答案
     */
    public String processCompletionAnswer(String answer) {
        if (answer == null || answer.isEmpty()) {
            return "";
        }

        // 如果是列表形式，拼接
        if (answer.contains(";") || answer.contains("；")) {
            String[] parts = answer.split("[;；]");
            StringBuilder result = new StringBuilder();
            for (String part : parts) {
                result.append(part.trim());
            }
            return result.toString();
        }

        return answer.trim();
    }

    /**
     * 生成随机单选题答案
     *
     * @param options 选项列表
     * @return 随机选项字母
     */
    public String randomSingleChoice(List<String> options) {
        if (options == null || options.isEmpty()) {
            return "A";
        }

        Random random = new Random();
        int index = random.nextInt(options.size());
        char letter = (char) ('A' + index);
        
        log.debug("随机单选题答案: {}", letter);
        return String.valueOf(letter);
    }

    /**
     * 生成随机多选题答案
     *
     * @param options 选项列表
     * @return 随机选项字母组合（2-3个）
     */
    public String randomMultipleChoice(List<String> options) {
        if (options == null || options.size() < 2) {
            return "AB";
        }

        Random random = new Random();
        int count = random.nextInt(2) + 2; // 2或3个选项
        count = Math.min(count, options.size());

        Set<Integer> selected = new TreeSet<>();
        while (selected.size() < count) {
            selected.add(random.nextInt(options.size()));
        }

        StringBuilder result = new StringBuilder();
        for (int idx : selected) {
            result.append((char) ('A' + idx));
        }

        log.debug("随机多选题答案: {}", result);
        return result.toString();
    }

    /**
     * 生成随机判断题答案
     *
     * @return "true" 或 "false"
     */
    public String randomJudgement() {
        Random random = new Random();
        return random.nextBoolean() ? "true" : "false";
    }

    /**
     * 检查字符串a是否是字符串b的子序列
     * 忽略大小写和特殊字符
     *
     * @param a 子序列
     * @param b 原字符串
     * @return 是否是子序列
     */
    public boolean isSubsequence(String a, String b) {
        if (a == null || b == null || a.isEmpty() || b.isEmpty()) {
            return false;
        }

        // 清理字符串，只保留字母、数字和中文
        String cleanA = keepAlphanumericAndChinese(a.toLowerCase());
        String cleanB = keepAlphanumericAndChinese(b.toLowerCase());

        if (cleanA.isEmpty() || cleanB.isEmpty()) {
            return false;
        }

        // 子序列匹配
        int i = 0, j = 0;
        while (i < cleanA.length() && j < cleanB.length()) {
            if (cleanA.charAt(i) == cleanB.charAt(j)) {
                i++;
            }
            j++;
        }

        return i == cleanA.length();
    }

    /**
     * 清理答案文本
     * 去除前后空白、特殊符号等
     *
     * @param answer 原始答案
     * @return 清理后的答案
     */
    private String cleanAnswer(String answer) {
        if (answer == null) {
            return "";
        }

        return answer.replaceAll("[\\r\\t\\n]", "")
                .replaceAll("^\\s+|\\s+$", "")
                .trim();
    }

    /**
     * 保留字母、数字和中文字符
     *
     * @param text 原始文本
     * @return 清理后的文本
     */
    private String keepAlphanumericAndChinese(String text) {
        if (text == null) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (Character.isLetterOrDigit(c) || isChinese(c)) {
                result.append(c);
            }
        }
        return result.toString();
    }

    /**
     * 判断字符是否为中文
     *
     * @param c 字符
     * @return 是否为中文
     */
    private boolean isChinese(char c) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
        return block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || block == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A;
    }

    /**
     * 分割多选题的多个答案
     *
     * @param answer 答案字符串
     * @return 答案列表
     */
    private List<String> splitMultipleAnswers(String answer) {
        List<String> result = new ArrayList<>();

        // 尝试多种分隔符
        String[] separators = {"\n", ";", "；", ",", "，", "、"};
        
        for (String sep : separators) {
            if (answer.contains(sep)) {
                String[] parts = answer.split(Pattern.quote(sep));
                for (String part : parts) {
                    String trimmed = part.trim();
                    if (!trimmed.isEmpty()) {
                        result.add(trimmed);
                    }
                }
                return result;
            }
        }

        // 没有分隔符，整个作为一个答案
        result.add(answer.trim());
        return result;
    }

    /**
     * 提取选项字母（从"A. xxx"中提取"A"）
     *
     * @param option 选项文本
     * @return 选项字母
     */
    private String extractOptionLetter(String option) {
        if (option == null || option.isEmpty()) {
            return null;
        }

        // 匹配开头的字母
        Pattern pattern = Pattern.compile("^([A-Za-z])[\\.、,，]");
        var matcher = pattern.matcher(option);
        
        if (matcher.find()) {
            return matcher.group(1).toUpperCase();
        }

        // 如果没有标点，直接返回第一个字符（如果是字母）
        char firstChar = option.charAt(0);
        if (Character.isLetter(firstChar)) {
            return String.valueOf(Character.toUpperCase(firstChar));
        }

        return null;
    }
}
