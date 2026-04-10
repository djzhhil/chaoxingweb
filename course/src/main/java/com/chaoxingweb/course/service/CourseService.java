package com.chaoxingweb.course.service;

import com.chaoxingweb.chaoxing.vo.ChapterVO;
import com.chaoxingweb.chaoxing.vo.CourseVO;

import java.util.List;
import java.util.Map;

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
    List<CourseVO> getCourseList();

    /**
     * 获取课程详情
     *
     * @param courseId 课程 ID
     * @return 课程详情
     */
    CourseVO getCourseDetail(String courseId);

    /**
     * 同步课程列表
     *
     * @return 同步的课程数量
     */
    int syncCourseList();

    /**
     * 获取课程章节列表（带持久化）
     *
     * @param courseId 课程ID
     * @param clazzId 班级ID
     * @param cpi CPI
     * @return 章节列表
     */
    List<ChapterVO> getChapterList(String courseId, String clazzId, String cpi);

    /**
     * 获取课程章节详情（带持久化）
     *
     * @param courseId 课程ID
     * @param clazzId 班级ID
     * @param cpi CPI
     * @return 章节详情
     */
    Map<String, Object> getChapterDetail(String courseId, String clazzId, String cpi);
}
