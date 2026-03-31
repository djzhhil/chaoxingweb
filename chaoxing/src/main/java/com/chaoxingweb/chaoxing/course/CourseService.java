package com.chaoxingweb.chaoxing.course;

import com.chaoxingweb.chaoxing.dto.CourseDTO;

import java.util.List;

/**
 * 课程服务接口
 *
 * @author 小克 🐕💎
 * @since 2026-03-31
 */
public interface CourseService {

    /**
     * 获取课程列表
     *
     * @return 课程列表
     */
    List<CourseDTO> getCourseList();

    /**
     * 获取课程章节
     *
     * @param courseId 课程 ID
     * @param clazzId 班级 ID
     * @param cpi CPI
     * @return 章节列表
     */
    List<CourseDTO> getCoursePoint(String courseId, String clazzId, String cpi);
}
