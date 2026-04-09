package com.chaoxingweb.course.service;

import com.chaoxingweb.chaoxing.vo.CourseVO;

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
}
