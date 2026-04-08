package com.chaoxingweb.chaoxing.adapter;

import com.chaoxingweb.chaoxing.dto.ChapterDTO;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 章节适配器
 *
 * 职责：
 * - 将超星 API 响应转换为内部模型
 * - 解析章节 HTML 响应
 * - 不包含业务逻辑
 *
 * @author 小克 🐕💎
 * @since 2026-04-08
 */
@Slf4j
@Component
public class ChapterAdapter {

    /**
     * 解析课程章节列表页面，提取章节点信息
     *
     * @param htmlText 章节列表页面的HTML内容
     * @param courseId 课程ID
     * @param clazzId 班级ID
     * @return 章节信息字典，包含是否锁定状态和章节点列表
     */
    public Map<String, Object> decodeCoursePoint(String htmlText, String courseId, String clazzId) {
        log.trace("开始解码章节列表...");
        
        Map<String, Object> coursePoint = new HashMap<>();
        coursePoint.put("hasLocked", false);  // 用于判断该课程任务是否是需要解锁
        
        List<ChapterDTO> points = new ArrayList<>();
        
        try {
            Document doc = Jsoup.parse(htmlText);
            Elements chapterUnits = doc.select("div.chapter_unit");
            
            for (Element chapterUnit : chapterUnits) {
                List<ChapterDTO> extractedPoints = extractPointsFromChapter(chapterUnit, courseId, clazzId);
                
                // 检查是否有锁定内容
                for (ChapterDTO point : extractedPoints) {
                    if (point.isNeedUnlock()) {
                        coursePoint.put("hasLocked", true);
                    }
                }
                
                points.addAll(extractedPoints);
            }
            
            coursePoint.put("points", points);
            log.info("解析到 {} 个章节点", points.size());
            
        } catch (Exception e) {
            log.error("解析章节列表异常", e);
        }
        
        return coursePoint;
    }

    /**
     * 从章节单元中提取章节点信息
     *
     * @param chapterUnit 章节单元元素
     * @param courseId 课程ID
     * @param clazzId 班级ID
     * @return 章节点信息列表
     */
    private List<ChapterDTO> extractPointsFromChapter(Element chapterUnit, String courseId, String clazzId) {
        List<ChapterDTO> pointList = new ArrayList<>();
        Elements rawPoints = chapterUnit.select("li");
        
        for (Element rawPoint : rawPoints) {
            Element point = rawPoint.selectFirst("div");
            if (point == null || !point.hasAttr("id")) {
                continue;
            }
            
            // 提取章节点ID
            String pointId = extractPointId(point.attr("id"));
            if (pointId == null) {
                continue;
            }
            
            // 提取章节标题
            Element titleElement = point.selectFirst("a.clicktitle");
            String pointTitle = titleElement != null ? 
                titleElement.text().replace("\n", "").trim() : "";
            
            // 提取任务数量
            int jobCount = 1;  // 默认为1
            Element jobCountElement = point.selectFirst("input.knowledgeJobCount");
            if (jobCountElement != null) {
                try {
                    jobCount = Integer.parseInt(jobCountElement.attr("value"));
                } catch (NumberFormatException e) {
                    log.warn("解析任务数量失败: {}", jobCountElement.attr("value"));
                }
            }
            
            // 检查是否需要解锁
            boolean needUnlock = false;
            Element hoverTips = point.selectFirst("span.bntHoverTips");
            if (hoverTips != null && hoverTips.text().contains("解锁")) {
                needUnlock = true;
            }
            
            // 判断是否已完成
            boolean hasFinished = false;
            if (hoverTips != null && hoverTips.text().contains("已完成")) {
                hasFinished = true;
            }
            
            // 创建ChapterDTO
            ChapterDTO chapterDTO = new ChapterDTO();
            chapterDTO.setId(pointId);
            chapterDTO.setTitle(pointTitle);
            chapterDTO.setCourseId(courseId);
            chapterDTO.setClazzId(clazzId);
            chapterDTO.setJobCount(jobCount);
            chapterDTO.setHasFinished(hasFinished);
            chapterDTO.setNeedUnlock(needUnlock);
            chapterDTO.setStatus(hasFinished ? "completed" : (needUnlock ? "locked" : "active"));
            
            pointList.add(chapterDTO);
        }
        
        return pointList;
    }

    /**
     * 从ID属性中提取章节点ID
     *
     * @param id ID属性值，格式如 "cur123456"
     * @return 提取的数字ID，如果格式不匹配返回null
     */
    private String extractPointId(String id) {
        Pattern pattern = Pattern.compile("^cur(\\d{1,20})$");
        Matcher matcher = pattern.matcher(id);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
