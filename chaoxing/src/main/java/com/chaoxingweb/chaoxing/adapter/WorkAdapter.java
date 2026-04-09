package com.chaoxingweb.chaoxing.adapter;

import com.chaoxingweb.chaoxing.dto.QuestionDTO;
import com.chaoxingweb.chaoxing.enums.QuestionType;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 测验题目适配器
 * 
 * 职责：
 * - 解析超星学习通测验页面HTML
 * - 提取题目信息和选项
 * - 处理字体加密（可选）
 * - 将原始HTML转换为QuestionDTO对象
 *
 * @author 小克 🐕💎
 * @since 2026-04-09
 */
@Slf4j
@Component
public class WorkAdapter {

    /**
     * 解析测验页面，提取题目信息
     *
     * @param htmlContent 测验页面HTML内容
     * @return 包含表单数据和题目列表的Map
     */
    public Map<String, Object> decodeQuestionsInfo(String htmlContent) {
        log.trace("开始解析测验题目信息...");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            Document doc = Jsoup.parse(htmlContent);
            
            // 1. 提取表单数据
            Map<String, String> formData = extractFormData(doc);
            result.putAll(formData);
            
            // 2. 检查是否有字体加密
            boolean hasFontEncryption = doc.getElementById("cxSecretStyle") != null;
            if (hasFontEncryption) {
                log.warn("检测到字体加密，当前版本暂不支持解密");
            }
            
            // 3. 提取所有题目
            List<QuestionDTO> questions = new ArrayList<>();
            Element form = doc.selectFirst("form");
            
            if (form == null) {
                log.error("未找到表单元素，无法解析题目");
                result.put("questions", questions);
                return result;
            }
            
            Elements questionDivs = form.select("div.singleQuesId");
            log.info("共找到{}道题目", questionDivs.size());
            
            for (Element divTag : questionDivs) {
                QuestionDTO question = processQuestion(divTag);
                if (question != null) {
                    questions.add(question);
                }
            }
            
            // 4. 更新表单数据
            result.put("questions", questions);
            
            // 生成answerwqbid字段（题目ID列表）
            String answerwqbid = questions.stream()
                    .map(QuestionDTO::getId)
                    .reduce("", (a, b) -> a + b + ",");
            result.put("answerwqbid", answerwqbid);
            
            log.info("题目解析完成，成功解析{}道题目", questions.size());
            
        } catch (Exception e) {
            log.error("解析测验题目异常", e);
        }
        
        return result;
    }

    /**
     * 从表单中提取数据
     *
     * @param doc HTML文档
     * @return 表单数据Map
     */
    private Map<String, String> extractFormData(Document doc) {
        Map<String, String> formData = new HashMap<>();
        Element form = doc.selectFirst("form");
        
        if (form == null) {
            return formData;
        }
        
        Elements inputs = form.select("input");
        for (Element input : inputs) {
            String name = input.attr("name");
            String value = input.attr("value");
            
            // 跳过答案字段（后续会填充）
            if (name == null || name.isEmpty() || name.contains("answer")) {
                continue;
            }
            
            formData.put(name, value != null ? value : "");
        }
        
        log.debug("提取到{}个表单字段", formData.size());
        return formData;
    }

    /**
     * 处理单个题目
     *
     * @param divTag 题目div元素
     * @return QuestionDTO对象
     */
    private QuestionDTO processQuestion(Element divTag) {
        try {
            // 提取题目ID
            String questionId = divTag.attr("data");
            
            // 提取题目类型代码
            Element tiMuDiv = divTag.selectFirst("div.TiMu");
            String typeCode = tiMuDiv != null ? tiMuDiv.attr("data") : "";
            QuestionType questionType = getQuestionType(typeCode);
            
            // 提取题目标题
            Element titleDiv = divTag.selectFirst("div.Zy_TItle");
            String title = extractTitle(titleDiv);
            
            // 提取选项列表
            List<String> options = extractOptions(divTag);
            
            // 构建答案字段映射
            Map<String, String> answerField = new HashMap<>();
            answerField.put("answer" + questionId, "");
            answerField.put("answertype" + questionId, typeCode);
            
            // 构建QuestionDTO
            QuestionDTO question = QuestionDTO.builder()
                    .id(questionId)
                    .title(title)
                    .type(questionType)
                    .options(options)
                    .answerField(answerField)
                    .answerSource("")
                    .queriedAnswer("")
                    .finalAnswer("")
                    .answered(false)
                    .build();
            
            log.debug("解析题目: ID={}, 类型={}, 标题={}", questionId, questionType, 
                    title.length() > 50 ? title.substring(0, 50) + "..." : title);
            
            return question;
            
        } catch (Exception e) {
            log.error("解析题目失败", e);
            return null;
        }
    }

    /**
     * 根据题型代码返回题型枚举
     *
     * @param typeCode 题型代码
     * @return QuestionType枚举
     */
    private QuestionType getQuestionType(String typeCode) {
        return switch (typeCode) {
            case "0" -> QuestionType.SINGLE;      // 单选题
            case "1" -> QuestionType.MULTIPLE;    // 多选题
            case "2" -> QuestionType.COMPLETION;  // 填空题
            case "3" -> QuestionType.JUDGEMENT;   // 判断题
            case "4" -> QuestionType.ESSAY;       // 简答题
            default -> {
                log.warn("未知题型代码: {}", typeCode);
                yield QuestionType.UNKNOWN;
            }
        };
    }

    /**
     * 提取题目标题
     *
     * @param titleDiv 标题div元素
     * @return 标题文本
     */
    private String extractTitle(Element titleDiv) {
        if (titleDiv == null) {
            return "";
        }
        
        // 获取所有文本内容
        StringBuilder content = new StringBuilder();
        for (Element child : titleDiv.children()) {
            content.append(child.text()).append(" ");
        }
        
        // 清理空白字符
        String cleaned = content.toString()
                .replace("\r", "")
                .replace("\t", "")
                .replace("\n", "")
                .trim();
        
        // 去除前缀（如【单选题】）
        cleaned = cleaned.replaceAll("^\\d+", "")
                .replaceAll("（\\d+\\.\\d+分）$", "")
                .trim();
        
        return cleaned;
    }

    /**
     * 提取选项列表
     *
     * @param divTag 题目div元素
     * @return 选项列表
     */
    private List<String> extractOptions(Element divTag) {
        List<String> options = new ArrayList<>();
        
        Element ul = divTag.selectFirst("ul");
        if (ul == null) {
            return options;
        }
        
        Elements lis = ul.select("li");
        for (Element li : lis) {
            String option = extractChoice(li);
            if (option != null && !option.isEmpty()) {
                options.add(option);
            }
        }
        
        // 排序选项（按字母顺序）
        Collections.sort(options);
        
        log.debug("提取到{}个选项", options.size());
        return options;
    }

    /**
     * 提取单个选项内容
     *
     * @param li 选项li元素
     * @return 选项文本
     */
    private String extractChoice(Element li) {
        if (li == null) {
            return "";
        }
        
        // 优先使用aria-label属性
        String choice = li.attr("aria-label");
        if (choice == null || choice.isEmpty()) {
            choice = li.text();
        }
        
        if (choice == null || choice.isEmpty()) {
            return "";
        }
        
        // 清理空白字符
        String cleaned = choice.replaceAll("[\\r\\t\\n]", "").trim();
        
        // 移除末尾的"选择"字样
        if (cleaned.endsWith("选择")) {
            cleaned = cleaned.substring(0, cleaned.length() - 2).trim();
        }
        
        return cleaned;
    }

    /**
     * 将选项列表转换为字符串（用换行符分隔）
     *
     * @param options 选项列表
     * @return 选项字符串
     */
    public String optionsToString(List<String> options) {
        if (options == null || options.isEmpty()) {
            return "";
        }
        return String.join("\n", options);
    }

    /**
     * 将选项字符串转换为列表
     *
     * @param optionsStr 选项字符串（换行符分隔）
     * @return 选项列表
     */
    public List<String> stringToOptions(String optionsStr) {
        if (optionsStr == null || optionsStr.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(optionsStr.split("\n"));
    }
}
