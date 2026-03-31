package com.chaoxingweb.app.controller;

import com.chaoxingweb.chaoxing.dto.CourseDTO;
import com.chaoxingweb.chaoxing.course.CourseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 课程控制器
 *
 * @author 小克 🐕💎
 * @since 2026-03-31
 */
@RestController
@RequestMapping("/api/course")
public class CourseController {

    private static final Logger logger = LoggerFactory.getLogger(CourseController.class);

    private final CourseService courseService;

    @Autowired
    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    /**
     * 获取课程列表
     *
     * @return 课程列表
     */
    @GetMapping("/list")
    public ResponseEntity<List<CourseDTO>> getCourseList() {
        try {
            logger.info("收到获取课程列表请求");
            List<CourseDTO> courseList = courseService.getCourseList();
            return ResponseEntity.ok(courseList);
        } catch (Exception e) {
            logger.error("获取课程列表失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
