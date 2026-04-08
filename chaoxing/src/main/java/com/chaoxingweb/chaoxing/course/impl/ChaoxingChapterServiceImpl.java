package com.chaoxingweb.chaoxing.course.impl;

import com.chaoxingweb.chaoxing.adapter.ChapterAdapter;
import com.chaoxingweb.chaoxing.client.ChaoxingApiClient;
import com.chaoxingweb.chaoxing.dto.ChapterDTO;
import com.chaoxingweb.chaoxing.course.ChaoxingChapterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 超星章节服务实现
 *
 * @author 小克 🐕💎
 * @since 2026-04-08
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChaoxingChapterServiceImpl implements ChaoxingChapterService {

    private final ChaoxingApiClient chaoxingApiClient;
    private final ChapterAdapter chapterAdapter;

    @Override
    public List<ChapterDTO> getChapterList(String courseId, String clazzId, String cpi) {
        try {
            log.info("开始获取课程章节列表: courseId={}, clazzId={}, cpi={}", courseId, clazzId, cpi);

            // 调用 API 客户端获取 HTML
            String html = chaoxingApiClient.getCoursePointHtml(courseId, clazzId, cpi);

            if (html == null || html.isEmpty()) {
                log.error("获取章节列表失败：HTML 为空");
                return new ArrayList<>();
            }

            // 使用适配器解析 HTML
            Map<String, Object> result = chapterAdapter.decodeCoursePoint(html, courseId, clazzId);
            
            // 提取章节列表
            @SuppressWarnings("unchecked")
            List<ChapterDTO> chapters = (List<ChapterDTO>) result.get("points");
            
            if (chapters == null) {
                log.warn("解析结果为空");
                return new ArrayList<>();
            }

            // 设置 CPI 信息
            chapters.forEach(chapter -> chapter.setCpi(cpi));

            log.info("章节列表获取成功，共{}个章节", chapters.size());
            return chapters;

        } catch (Exception e) {
            log.error("获取章节列表异常", e);
            return new ArrayList<>();
        }
    }

    @Override
    public Map<String, Object> getChapterDetail(String courseId, String clazzId, String cpi) {
        try {
            log.info("开始获取课程章节详情: courseId={}, clazzId={}, cpi={}", courseId, clazzId, cpi);

            // 调用 API 客户端获取 HTML
            String html = chaoxingApiClient.getCoursePointHtml(courseId, clazzId, cpi);

            if (html == null || html.isEmpty()) {
                log.error("获取章节详情失败：HTML 为空");
                return Map.of("hasLocked", false, "points", new ArrayList<>());
            }

            // 使用适配器解析 HTML
            Map<String, Object> result = chapterAdapter.decodeCoursePoint(html, courseId, clazzId);
            
            // 为所有章节设置 CPI 信息
            @SuppressWarnings("unchecked")
            List<ChapterDTO> chapters = (List<ChapterDTO>) result.get("points");
            if (chapters != null) {
                chapters.forEach(chapter -> chapter.setCpi(cpi));
            }

            log.info("章节详情获取成功");
            return result;

        } catch (Exception e) {
            log.error("获取章节详情异常", e);
            return Map.of("hasLocked", false, "points", new ArrayList<>());
        }
    }
}
