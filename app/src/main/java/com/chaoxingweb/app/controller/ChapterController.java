package com.chaoxingweb.app.controller;

import com.chaoxingweb.chaoxing.vo.ChapterVO;
import com.chaoxingweb.course.service.CourseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 章节控制器
 *
 * @author 小克 🐕💎
 * @since 2026-04-08
 */
@RestController
@RequestMapping("/api/chapter")
public class ChapterController {

    private static final Logger logger = LoggerFactory.getLogger(ChapterController.class);

    private final CourseService courseService;

    @Autowired
    public ChapterController(CourseService courseService) {
        this.courseService = courseService;
    }

    /**
     * 获取课程章节列表
     *
     * @param courseId 课程ID
     * @param clazzId 班级ID
     * @param cpi CPI
     * @return 章节列表
     */
    @GetMapping("/list")
    public ResponseEntity<List<ChapterVO>> getChapterList(
            @RequestParam String courseId,
            @RequestParam String clazzId,
            @RequestParam String cpi) {
        try {
            logger.info("收到获取章节列表请求: courseId={}, clazzId={}, cpi={}", courseId, clazzId, cpi);
            List<ChapterVO> chapterList = courseService.getChapterList(courseId, clazzId, cpi);
            return ResponseEntity.ok(chapterList);
        } catch (Exception e) {
            logger.error("获取章节列表失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取课程章节详情（包含锁定状态等信息）
     *
     * @param courseId 课程ID
     * @param clazzId 班级ID
     * @param cpi CPI
     * @return 章节详情
     */
    @GetMapping("/detail")
    public ResponseEntity<Map<String, Object>> getChapterDetail(
            @RequestParam String courseId,
            @RequestParam String clazzId,
            @RequestParam String cpi) {
        try {
            logger.info("收到获取章节详情请求: courseId={}, clazzId={}, cpi={}", courseId, clazzId, cpi);
            Map<String, Object> chapterDetail = courseService.getChapterDetail(courseId, clazzId, cpi);
            return ResponseEntity.ok(chapterDetail);
        } catch (Exception e) {
            logger.error("获取章节详情失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
