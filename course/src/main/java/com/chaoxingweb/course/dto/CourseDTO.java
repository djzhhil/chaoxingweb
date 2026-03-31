package com.chaoxingweb.course.dto;

import lombok.Data;

/**
 * 课程 DTO
 *
 * @author 小克 🐕💎
 * @since 2026-03-31
 */
@Data
public class CourseDTO {

    /**
     * 课程 ID
     */
    private String courseId;

    /**
     * 班级 ID
     */
    private String clazzId;

    /**
     * CPI
     */
    private String cpi;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 教师名称
     */
    private String teacherName;

    /**
     * 学校名称
     */
    private String schoolName;

    /**
     * 课程描述
     */
    private String description;

    /**
     * 课程封面
     */
    private String coverUrl;

    /**
     * 课程状态
     */
    private String status;
}
